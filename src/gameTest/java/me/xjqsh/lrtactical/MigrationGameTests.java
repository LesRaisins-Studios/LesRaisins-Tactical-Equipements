package me.xjqsh.lrtactical;

import com.tacz.guns.api.TimelessAPI;

import io.netty.buffer.Unpooled;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.index.ICustomItemIndex;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;
import me.xjqsh.lrtactical.entity.GrenadeEntity;
import me.xjqsh.lrtactical.init.ModEnchantment;
import me.xjqsh.lrtactical.init.ModItems;
import me.xjqsh.lrtactical.network.DataType;
import me.xjqsh.lrtactical.network.message.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@GameTestHolder(EquipmentMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MigrationGameTests {
    @GameTest(template = "empty")
    public static void equipmentSurvivesSaveAndReload(GameTestHelper helper) {
        var indexes = new ArrayList<ICustomItemIndex>();
        indexes.addAll(LrTacticalAPI.getThrowableIndexes());
        indexes.addAll(LrTacticalAPI.getConsumableIndexes());
        indexes.addAll(LrTacticalAPI.getMeleeIndexes());
        helper.assertValueEqual(indexes.size(), 19, "built-in equipment count");
        for (var index : indexes) {
            var stack = index.createItemStack();
            var copy =
                    ItemStack.parse(
                                    helper.getLevel().registryAccess(),
                                    stack.save(helper.getLevel().registryAccess()))
                            .orElseThrow();
            helper.assertValueEqual(
                    ((ICustomItem) copy.getItem()).getId(copy),
                    index.getId(),
                    "saved equipment ID");
            helper.assertValueEqual(
                    copy.getMaxStackSize(), index.getMaxStackSize(), "saved stack limit");
            helper.assertTrue(
                    ItemStack.isSameItemSameComponents(stack, copy),
                    "Components changed across serialization: " + index.getId());
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recipesKeepCustomIdentity(GameTestHelper helper) {
        var recipeManager = helper.getLevel().getRecipeManager();
        var registryAccess = helper.getLevel().registryAccess();
        for (String path : List.of("m67", "rgn")) {
            var itemId = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, path);
            var recipeId = itemId.withPrefix("throwable/");
            var recipe = recipeManager.byKey(recipeId).orElseThrow();
            var result = recipe.value().getResultItem(registryAccess);
            helper.assertValueEqual(((ICustomItem) result.getItem()).getId(result), itemId, "recipe equipment ID");
            helper.assertValueEqual(result.getCount(), 3, "recipe output count");
            helper.assertValueEqual(result.get(DataComponents.MAX_STACK_SIZE), 6, "recipe output stack component");
        }
        var maxDamageByRecipe = Map.of("consumable/cms", 5, "melee/dagger", 384);
        for (var expected : maxDamageByRecipe.entrySet()) {
            var recipeId = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, expected.getKey());
            var recipe = recipeManager.byKey(recipeId).orElseThrow();
            var result = recipe.value().getResultItem(registryAccess);
            helper.assertValueEqual(result.get(DataComponents.MAX_DAMAGE), expected.getValue(), recipeId + " max damage");
            helper.assertValueEqual(result.get(DataComponents.DAMAGE), 0, recipeId + " initial damage");
        }
        helper.assertTrue(
                recipeManager.byKey(ResourceLocation.parse("lrtactical:smith_table")).isPresent(),
                "Workbench recipe missing");
        helper.assertTrue(
                TimelessAPI.getCommonBlockIndex(ResourceLocation.parse("lrtactical:smith_table"))
                        .isPresent(),
                "Equipment workbench data failed to load");
        helper.assertValueEqual(
                recipeManager.getRecipes().stream()
                        .filter(holder -> holder.id().getNamespace().equals("lrtactical"))
                        .count(),
                20L,
                "built-in recipe count");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void combatAttachmentsAndCriticalHits(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var combat = CombatProperties.get(player).orElseThrow();
        helper.assertTrue(
                combat == CombatProperties.get(player).orElseThrow(),
                "Combat attachment recreated");
        helper.assertTrue(
                CombatProperties.get(helper.spawn(EntityType.ARMOR_STAND, 1, 1, 1)).isEmpty(),
                "Non-player received combat state");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FLASH_SHIELD.get()));
        player.disableShield();
        helper.assertTrue(
                CustomItemCoolDowns.get(player)
                        .orElseThrow()
                        .isOnCooldown(ResourceLocation.parse("shield_disabled")),
                "Shield mixin did not apply custom cooldown");
        var target = helper.spawn(EntityType.ZOMBIE, 1, 1, 1);
        var stack = LrTacticalAPI.getMeleeIndexes().iterator().next().createItemStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var weapon = (IMeleeWeapon) stack.getItem();
        player.fallDistance = 0;
        helper.assertTrue(
                !weapon.performAttack(player, target, stack, 1, 0).crit(),
                "Normal attack incorrectly counted as critical");
        target.invulnerableTime = 0;
        player.fallDistance = 1;
        player.setOnGround(false);
        helper.assertTrue(
                weapon.performAttack(player, target, stack, 1, 0).crit(),
                "Falling attack lost critical status");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void payloadRoundTrip(GameTestHelper helper) {
        roundTrip(
                helper,
                CPrepareMeleeAttack.STREAM_CODEC,
                new CPrepareMeleeAttack(MeleeAction.LEFT, new Vec3(1, 2, 3), new Vec3(0, 0, 1)));
        roundTrip(
                helper,
                CMeleeAttackRequest.STREAM_CODEC,
                new CMeleeAttackRequest(MeleeAction.LEFT, 2, new int[] {1, 42}));
        roundTrip(
                helper, CCancelToggleConsumableUse.STREAM_CODEC, new CCancelToggleConsumableUse());
        roundTrip(
                helper,
                SCustomSound.STREAM_CODEC,
                new SCustomSound(
                        SCustomSound.SoundType.MELEE,
                        ResourceLocation.parse("lrtactical:karambit"),
                        "attack_left",
                        Vec3.ZERO,
                        2,
                        1));
        roundTrip(
                helper,
                SCustomCoolDownMessage.STREAM_CODEC,
                new SCustomCoolDownMessage(ResourceLocation.parse("lrtactical:medical"), 100));
        roundTrip(
                helper,
                SPackSyncMessage.STREAM_CODEC,
                new SPackSyncMessage(
                        Map.of(
                                DataType.CONSUMABLE_INDEX,
                                Map.of(ResourceLocation.parse("lrtactical:cms"), "{}"))));
        roundTrip(
                helper,
                SMeleeAnimationSync.STREAM_CODEC,
                new SMeleeAnimationSync(
                        1, MeleeAction.LEFT, 2, ResourceLocation.parse("lrtactical:karambit")));
        roundTrip(
                helper,
                SMeleeAnimationSync.STREAM_CODEC,
                new SMeleeAnimationSync(1, MeleeAction.LEFT, 2, null));
        roundTrip(helper, SResetMeleeSyncMessage.STREAM_CODEC, new SResetMeleeSyncMessage());
        roundTrip(
                helper,
                SShakeScreenMessage.STREAM_CODEC,
                new SShakeScreenMessage(20, 10, 60, Vec3.ZERO));
        roundTrip(helper, SShieldDisable.STREAM_CODEC, new SShieldDisable());
        roundTrip(helper, SShieldShake.STREAM_CODEC, new SShieldShake());
        roundTrip(
                helper, SSplashParticle.STREAM_CODEC, new SSplashParticle(BlockPos.ZERO, 0x123456));
        helper.succeed();
    }

    private static <T extends CustomPacketPayload> void roundTrip(
            GameTestHelper helper, StreamCodec<FriendlyByteBuf, T> codec, T packet) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        var encodedAgain = new FriendlyByteBuf(Unpooled.buffer());
        try {
            codec.encode(buffer, packet);
            byte[] original = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), original);
            var decoded = codec.decode(buffer);
            helper.assertValueEqual(decoded.type(), packet.type(), "payload type");
            helper.assertValueEqual(buffer.readableBytes(), 0, "unread payload bytes");
            codec.encode(encodedAgain, decoded);
            byte[] actual = new byte[encodedAgain.readableBytes()];
            encodedAgain.readBytes(actual);
            helper.assertTrue(
                    Arrays.equals(original, actual),
                    "Payload changed during round-trip: " + packet.type().id());
        } finally {
            buffer.release();
            encodedAgain.release();
        }
    }

    @GameTest(template = "empty")
    public static void durabilityAndThrowableSpawnData(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var cms =
                LrTacticalAPI.getConsumableIndexes().stream()
                        .filter(
                                index ->
                                        index.getId()
                                                .equals(ResourceLocation.parse("lrtactical:cms")))
                        .findFirst()
                        .orElseThrow()
                        .createItemStack();
        helper.assertValueEqual(cms.getMaxDamage(), 5, "consumable durability");
        cms.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        helper.assertValueEqual(cms.getDamageValue(), 1, "durability consumed");
        var index =
                LrTacticalAPI.getThrowableIndexes().stream()
                        .filter(
                                value ->
                                        value.getId()
                                                .equals(ResourceLocation.parse("lrtactical:m67")))
                        .findFirst()
                        .orElseThrow();
        var thrown = index.createEntity(index.createItemStack(), player);
        var copy = new GrenadeEntity(GrenadeEntity.TYPE, helper.getLevel());
        var buffer =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            thrown.writeSpawnData(buffer);
            copy.readSpawnData(buffer);
            helper.assertValueEqual(copy.getLife(), thrown.getLife(), "spawn fuse time");
            helper.assertValueEqual(
                    copy.getDefaultGravity(), thrown.getDefaultGravity(), "spawn gravity");
            helper.assertValueEqual(
                    copy.getTailParticle().getType(),
                    thrown.getTailParticle().getType(),
                    "spawn particle type");
            helper.assertValueEqual(buffer.readableBytes(), 0, "unread spawn bytes");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void backstabRemainsAvailableAtEnchantingTable(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var backstab = enchantments.getOrThrow(ModEnchantment.BACKSTAB);
        var stack = LrTacticalAPI.getMeleeIndexes().iterator().next().createItemStack();
        helper.assertTrue(backstab.value().canEnchant(stack), "Backstab rejected a melee weapon");
        helper.assertTrue(
                backstab.value().canEnchant(new ItemStack(Items.IRON_SWORD)),
                "Backstab rejected a vanilla sword");
        helper.assertValueEqual(backstab.value().getMaxLevel(), 2, "backstab maximum level");
        helper.assertTrue(
                backstab.is(EnchantmentTags.IN_ENCHANTING_TABLE),
                "Backstab missing from enchanting table pool");
        helper.succeed();
    }
}
