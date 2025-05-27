package me.xjqsh.lrtactical.api.item;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import java.util.Objects;
import java.util.Optional;

public interface IMeleeWeapon extends ICustomItem {
    String ID_TAG = "MeleeWeaponId";
    String OVERRIDE_DISPLAY_ID = "DisplayId";
    ResourceLocation EMPTY = new ResourceLocation(EquipmentMod.MOD_ID, "empty");

    static IMeleeWeapon of(ItemStack stack) {
        if (stack.getItem() instanceof IMeleeWeapon item) {
            return item;
        }
        return null;
    }

    @Override
    default ResourceLocation getId(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(ID_TAG, Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(nbt.getString(ID_TAG));
            return Objects.requireNonNullElse(rl, EMPTY);
        }
        return EMPTY;
    }

    @Override
    default ResourceLocation getDisplayId(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(OVERRIDE_DISPLAY_ID, Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(nbt.getString(OVERRIDE_DISPLAY_ID));
            return Objects.requireNonNullElse(rl, EMPTY);
        }
        return getId(stack);
    }

    @Override
    default void setId(ItemStack stack, ResourceLocation id) {
        stack.getOrCreateTag().putString(ID_TAG, id.toString());
    }

    @Override
    default boolean shouldBlockAttack() {
        return true;
    }

    @Override
    default boolean shouldBlockUse() {
        return true;
    }

    default int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action) {
        return 0;
    }

    default boolean canAttack(Player attacker, ItemStack stack, MeleeAction action) {
        return true;
    }

    default void attack(Player attacker, ItemStack stack, MeleeAction action) {
        this.attack(attacker, stack, action, attacker.position(), attacker.getLookAngle());
    }

    default Optional<MeleeWeaponIndex<?>> getMeleeIndex(ItemStack stack) {
        return LrTacticalAPI.getMeleeIndex(stack);
    }

    void attack(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction);

    default void performAttack(Player attacker, Entity target, ItemStack stack, float base, float knockback) {
        // forge事件
        if (!ForgeHooks.onPlayerAttackTarget(attacker, target)) return;
        if (!target.isAttackable()) return;
        if (target.skipAttackInteraction(attacker)) return;

        float modifier;
        if (target instanceof LivingEntity living) {
            modifier = EnchantmentHelper.getDamageBonus(stack, living.getMobType());
        } else {
            modifier = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
        }
        if (target instanceof LivingEntity living) {
            living.knockback(knockback, Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F)));
        }

        boolean flag2 = attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable() && !attacker.isInWater()
                && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && target instanceof LivingEntity;

        // 原版跳劈暴击
        CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(attacker, target, flag2, flag2 ? 1.5F : 1.0F);
        if (hitResult != null) {
            base *= hitResult.getDamageModifier();
            flag2 = true;
        }

        int j = EnchantmentHelper.getFireAspect(attacker);
        if (target instanceof LivingEntity living) {
            if (j > 0) {
                living.setSecondsOnFire(j * 4);
            }
        }

        target.invulnerableTime = 0;
        target.hurt(attacker.damageSources().playerAttack(attacker), base + modifier);

        if (target instanceof LivingEntity living) {
            EnchantmentHelper.doPostHurtEffects(living, attacker);
        }

        EnchantmentHelper.doPostDamageEffects(attacker, target);

        if (flag2) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
            attacker.crit(target);
        }
    }

    default boolean canSprintingAttack() {
        return true;
    }
}
