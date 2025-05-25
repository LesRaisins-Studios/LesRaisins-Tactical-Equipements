package me.xjqsh.lrtactical.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.tacz.guns.api.item.IAnimationItem;
import me.xjqsh.lrtactical.api.collision.ConeFilter;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import me.xjqsh.lrtactical.capability.CustomItemCoolDownsProvider;
import me.xjqsh.lrtactical.client.renderer.item.FlashShieldItemRenderer;
import me.xjqsh.lrtactical.config.ServerConfig;
import me.xjqsh.lrtactical.init.ModEffects;
import me.xjqsh.lrtactical.item.throwable.flash.StunThrowableData;
import me.xjqsh.lrtactical.util.SightTraceUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

public class FlashShieldItem extends Item implements IMeleeWeapon, IAnimationItem {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public FlashShieldItem() {
        super(new Properties().stacksTo(1).durability(350));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier("Shield modifier", -0.25, AttributeModifier.Operation.MULTIPLY_BASE));
        defaultModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            return defaultModifiers;
        }
        return ImmutableMultimap.of();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ServerConfig.FLASH_SHIELD_MAX_DURABILITY.get();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final FlashShieldItemRenderer renderer = new FlashShieldItemRenderer();

            @Override
            public FlashShieldItemRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        });
    }

    @Override
    public int getDrawTime(ItemStack stack) {
        return 10;
    }

    @Override
    public void attack(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction) {
        ConeFilter filter = new ConeFilter(2.5f, 105);
        float base = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (Entity livingentity : filter.filterTargets(attacker, origin, direction)) {
            boolean flag = !(livingentity instanceof ArmorStand armorStand) || !armorStand.isMarker();

            if (livingentity != attacker && !attacker.isAlliedTo(livingentity) && flag) {
                this.performAttack(attacker, livingentity, stack, base, 1.2f);
            }
        }
    }

    @Override
    public boolean canSprintingAttack() {
        return false;
    }

    @Override
    public int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action) {
        return 5;
    }

    @Override
    public boolean canAttack(Player attacker, ItemStack stack, MeleeAction action) {
        boolean isDisabled = attacker.getCapability(CustomItemCoolDownsProvider.CAPABILITY)
                .map(cap -> cap.isOnCooldown(new ResourceLocation("shield_disabled")))
                .orElse(false);
        return action == MeleeAction.LEFT && !isDisabled;
    }

    @Override
    public boolean shouldBlockUse() {
        return false;
    }

    @Override
    public int getMaxUsingTick(ItemStack stack) {
        return 10;
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getUseDuration(ItemStack pStack) {
        return 10;
    }

    @ParametersAreNonnullByDefault
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.fail(player.getItemInHand(pUsedHand));
        }
        boolean coolDown = player.getCapability(CombatPropertiesProvider.CAPABILITY)
                .map(cap -> cap.getCoolDownTick() > 0)
                .orElse(false);
        boolean isDisabled = player.getCapability(CustomItemCoolDownsProvider.CAPABILITY)
                .map(cap -> cap.isOnCooldown(new ResourceLocation("shield_disabled")))
                .orElse(false);
        if (coolDown || isDisabled) {
            return InteractionResultHolder.fail(player.getItemInHand(pUsedHand));
        }
        ItemStack stack = player.getItemInHand(pUsedHand);
        player.startUsingItem(pUsedHand);
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }


    @ParametersAreNonnullByDefault
    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (entity.getTicksUsingItem() >= this.getMaxUsingTick(stack)) {
            if (!world.isClientSide()) {
                if (entity instanceof Player player) {
                    player.getCooldowns().addCooldown(stack.getItem(), ServerConfig.FLASH_SHIELD_COOLDOWN.get());
                    player.addEffect(new MobEffectInstance(ModEffects.BLIND.get(), 45, 0, false, false));
                    player.addEffect(new MobEffectInstance(ModEffects.DEAFENED.get(), 60, 0, false, false));
                }

                AABB aabb = entity.getBoundingBox().inflate(12);
                for (Entity target : world.getEntities(entity, aabb, EntitySelector.NO_SPECTATORS)) {
                    if (target instanceof LivingEntity living) {
                        calculateAndApplyEffect(entity, living, data);
                    }
                }

            }
        }
        return stack;
    }

    public static StunThrowableData.StunData data = new StunThrowableData.StunData();

    public static void calculateAndApplyEffect(Entity starter, LivingEntity target, StunThrowableData.StunData data) {
        // origin position
        Vec3 p = starter.position().add(0.0,1.0,0.0);
        // target eyes
        Vec3 eyes = target.getEyePosition(1.0F);
        // f to t
        Vec3 d1 = p.subtract(eyes);
        // t to f
        Vec3 d2 = eyes.subtract(p);

        double distanceMax = data.getRadius();
        double distance = d1.length();

        if(distance > distanceMax){
            return;
        }

        // Calculate angle between eye-gaze line and eye-grenade line
        // 目标视线与两点连线的夹角
        double a1 = Math.toDegrees(Math.acos(target.getViewVector(1.0F).dot(d1.normalize())));
        // 释放者视线与两点连线的夹角
        double a2 = Math.toDegrees(Math.acos(starter.getViewVector(1.0F).dot(d2.normalize())));
        // 目标的视线范围
        double angleMax = data.getBlind().getMaxAngle();

        if(a1 > 0 && a1 < angleMax && a2 > 0 && a2 <= 90.0){
            if(SightTraceUtil.rayTraceOpaqueBlocks(starter, target.level(), eyes, p, false, false, false) == null) {
                // Duration attenuated by distance
                int durationBlinded = data.calcBlindDuration(distance, a1);
                if (durationBlinded > 0){
                    target.addEffect(new MobEffectInstance(ModEffects.BLIND.get(), durationBlinded, 0, false, false, true));
                }
            }
        }

        int durationDeafened = data.calcDeafenedDuration(distance);
        if (durationDeafened > 0){
            target.addEffect(new MobEffectInstance(ModEffects.DEAFENED.get(), durationDeafened, 0, false, false, true));
        }
    }

    @Override
    public boolean useOnRelease(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItem(stack1, stack2);
    }
}
