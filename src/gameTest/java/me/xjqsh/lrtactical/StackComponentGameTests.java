package me.xjqsh.lrtactical;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.tacz.guns.resource.convert.LegacyItemStackUpgrader;
import io.netty.buffer.Unpooled;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.init.ModItems;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;


@GameTestHolder(EquipmentMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StackComponentGameTests {
    @GameTest(template = "empty")
    public static void convertedEquipmentConsumesDurability(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var cms = convert(helper, """
                {"item":"lrtactical:consumable","nbt":{"ConsumableId":"lrtactical:cms"}}
                """);
        helper.assertTrue(cms.isDamageableItem(), "Converted CMS is not damageable");
        helper.assertValueEqual(cms.get(DataComponents.MAX_DAMAGE), 5, "CMS native max damage");
        cms.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        helper.assertValueEqual(cms.getDamageValue(), 1, "CMS durability consumption");

        var usedCms = convert(helper, """
                {"item":"lrtactical:consumable","nbt":{"ConsumableId":"lrtactical:cms","Damage":2}}
                """);
        helper.assertValueEqual(usedCms.getDamageValue(), 2, "Existing CMS damage");
        usedCms.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        helper.assertValueEqual(usedCms.getDamageValue(), 3, "Used CMS durability consumption");

        var melee = convert(helper, """
                {"item":"lrtactical:melee","nbt":{"MeleeWeaponId":"lrtactical:karambit"}}
                """);
        helper.assertTrue(melee.isDamageableItem(), "Converted melee weapon is not damageable");
        helper.assertValueEqual(melee.get(DataComponents.MAX_DAMAGE), 384, "Melee native max damage");
        melee.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        helper.assertValueEqual(melee.getDamageValue(), 1, "Melee durability consumption");

        var throwable = convert(helper, """
                {"item":"lrtactical:throwable","count":2,"nbt":{"ThrowableId":"lrtactical:m67"}}
                """);
        helper.assertValueEqual(throwable.get(DataComponents.MAX_STACK_SIZE), 3, "Throwable native stack size");
        helper.assertValueEqual(throwable.getCount(), 2, "Converted throwable count");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void convertedAndFactoryStacksMerge(GameTestHelper helper) {
        var converted = convert(helper, """
                {"item":"lrtactical:throwable","nbt":{"ThrowableId":"lrtactical:m67"}}
                """);
        var index = ((IThrowable) converted.getItem()).getThrowableIndex(converted).orElseThrow();
        var created = index.createItemStack();
        helper.assertTrue(ItemStack.isSameItemSameComponents(converted, created),
                "Converted and factory items have different components");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, created);
        player.getInventory().add(converted.copy());
        helper.assertValueEqual(player.getInventory().getItem(0).getCount(), 2,
                "Converted item did not merge with the factory item");
        helper.assertTrue(player.getInventory().getItem(1).isEmpty(),
                "Converted item used a second inventory slot");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void changingEquipmentIdUpdatesComponents(GameTestHelper helper) {
        var stack = new ItemStack(ModItems.CONSUMABLE.get());
        var item = (IConsumable) stack.getItem();
        item.setId(stack, ResourceLocation.parse("lrtactical:cms"));
        helper.assertTrue(stack.isDamageableItem(), "Setting an ID did not initialize durability");
        stack.setDamageValue(2);
        item.setId(stack, ResourceLocation.parse("lrtactical:cms"));
        helper.assertValueEqual(stack.getDamageValue(), 2, "Repeated ID assignment reset damage");

        item.setId(stack, ResourceLocation.parse("lrtactical:blood_pack"));
        helper.assertTrue(!stack.has(DataComponents.MAX_DAMAGE), "Non-durable item retained max damage");
        helper.assertTrue(!stack.has(DataComponents.DAMAGE), "Non-durable item retained stale damage");
        helper.assertValueEqual(stack.get(DataComponents.MAX_STACK_SIZE), 2, "Blood pack stack size");
        helper.assertTrue(!stack.isDamageableItem(), "Blood pack became damageable");

        var throwable = new ItemStack(ModItems.THROWABLE.get());
        ((IThrowable) throwable.getItem()).setId(throwable, ResourceLocation.parse("lrtactical:m67"));
        helper.assertValueEqual(throwable.get(DataComponents.MAX_STACK_SIZE), 3, "Throwable ID assignment");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void copySaveAndNetworkPreserveDamage(GameTestHelper helper) {
        var stack = convert(helper, """
                {"item":"lrtactical:consumable","nbt":{"ConsumableId":"lrtactical:cms","Damage":2}}
                """);
        stack.getItem().verifyComponentsAfterLoad(stack);
        stack.getItem().verifyComponentsAfterLoad(stack);
        helper.assertValueEqual(stack.getDamageValue(), 2, "Repeated verification reset damage");
        var copied = stack.copy();
        helper.assertTrue(ItemStack.isSameItemSameComponents(stack, copied), "Copy changed components");
        var saved = ItemStack.parse(helper.getLevel().registryAccess(), stack.save(helper.getLevel().registryAccess()))
                .orElseThrow();
        helper.assertValueEqual(saved.getDamageValue(), 2, "Save round-trip reset damage");

        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            ItemStack.STREAM_CODEC.encode(buffer, stack);
            var received = ItemStack.STREAM_CODEC.decode(buffer);
            helper.assertTrue(received.isDamageableItem(), "Network stack lost durability components");
            helper.assertValueEqual(received.getDamageValue(), 2, "Network round-trip reset damage");
        } finally {
            buffer.release();
        }
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        stack.getItem().verifyComponentsAfterLoad(stack);
        helper.assertTrue(stack.has(DataComponents.UNBREAKABLE), "Verification removed unbreakable flag");
        helper.assertTrue(!stack.isDamageableItem(), "Unbreakable item became damageable");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void applyingComponentsTriggersVerification(GameTestHelper helper) {
        var stack = new ItemStack(ModItems.CONSUMABLE.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("ConsumableId", "lrtactical:cms");
        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
                .build());
        helper.assertTrue(stack.isDamageableItem(), "Component application did not initialize durability");
        stack.setDamageValue(2);
        stack.applyComponentsAndValidate(DataComponentPatch.builder()
                .remove(DataComponents.MAX_DAMAGE)
                .build());
        helper.assertValueEqual(stack.get(DataComponents.MAX_DAMAGE), 5, "Missing max damage was not repaired");
        helper.assertValueEqual(stack.getDamageValue(), 2, "Component repair reset damage");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unknownEquipmentKeepsExistingComponents(GameTestHelper helper) {
        var stack = ItemStack.CODEC.parse(helper.getLevel().registryAccess().createSerializationContext(
                JsonOps.INSTANCE), JsonParser.parseString("""
                {"id":"lrtactical:consumable","components":{
                  "minecraft:custom_data":{"ConsumableId":"lrtactical:missing_component_test"},
                  "minecraft:max_damage":19,"minecraft:damage":3}}
                """)).getOrThrow();
        stack.getItem().verifyComponentsAfterLoad(stack);
        helper.assertValueEqual(stack.get(DataComponents.MAX_DAMAGE), 19, "Unknown ID changed max damage");
        helper.assertValueEqual(stack.get(DataComponents.DAMAGE), 3, "Unknown ID erased damage");
        helper.succeed();
    }

    private static ItemStack convert(GameTestHelper helper, String input) {
        var upgrader = new LegacyItemStackUpgrader(helper.getLevel().registryAccess());
        var converted = upgrader.upgrade(JsonParser.parseString(input).getAsJsonObject());
        return ItemStack.CODEC.parse(upgrader.ops(), converted).getOrThrow();
    }
}
