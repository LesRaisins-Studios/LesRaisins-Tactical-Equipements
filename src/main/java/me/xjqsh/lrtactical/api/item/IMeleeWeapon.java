package me.xjqsh.lrtactical.api.item;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.melee.AttackResult;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.config.CommonConfig;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.melee.CombatData;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SCustomSound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

    /**
     * 获取攻击的位移信息
     * @param entity 攻击者
     * @param stack 攻击使用的物品
     * @param action 攻击动作
     * @return 位移信息
     */
    @Nullable
    default CombatData.MeleeMovement getAttackMovement(Player entity, ItemStack stack, MeleeAction action) {
        return null;
    }

    default boolean canAttack(Player attacker, ItemStack stack, MeleeAction action) {
        return true;
    }

    default Optional<MeleeWeaponIndex<?>> getMeleeIndex(ItemStack stack) {
        return LrTacticalAPI.getMeleeIndex(stack);
    }

    /**
     * 应在客户端进行。根据攻击信息进行索敌
     * @param attacker 攻击者
     * @param stack 攻击使用的物品
     * @param action 攻击动作
     * @return 受到攻击的实体列表
     */
    List<Entity> collectTargets(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction);

    /**
     * 应在服务端进行。根据攻击信息和索敌结果执行攻击逻辑
     * @param attacker 攻击者
     * @param stack 攻击使用的物品
     * @param action 攻击动作
     */
    void attack(Player attacker, ItemStack stack, MeleeAction action, List<Entity> targets);

    @Deprecated
    default void attack(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction) {}

    /**
     * 对特定的目标执行攻击逻辑
     * @param attacker 攻击者
     * @param target 攻击目标
     * @param stack 攻击使用的物品
     * @param base 基础伤害
     * @param knockback 击退强度
     * @return 是否成功攻击
     */
    default AttackResult performAttack(Player attacker, Entity target, ItemStack stack, float base, float knockback) {
        // forge事件
        if (!ForgeHooks.onPlayerAttackTarget(attacker, target)) return AttackResult.MISS;
        if (!target.isAttackable()) return AttackResult.MISS;
        if (target.skipAttackInteraction(attacker)) return AttackResult.MISS;

        float modifier;
        if (target instanceof LivingEntity living) {
            modifier = EnchantmentHelper.getDamageBonus(stack, living.getMobType());
        } else {
            modifier = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
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

        if (target.invulnerableTime < CommonConfig.MELEE_IGNORE_INVULNERABLE_TICK_THRESHOLD.get()) {
            target.invulnerableTime = 0;
        }

        boolean result = target.hurt(attacker.damageSources().playerAttack(attacker), base + modifier);
        // 如果目标实体实际没有受到攻击，则不应用其他效果了，比如击退和附魔后效等
        if (!result) {
            return AttackResult.MISS;
        }

        if (target instanceof LivingEntity living) {
            living.knockback(knockback, Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F)));
            EnchantmentHelper.doPostHurtEffects(living, attacker);
        }

        EnchantmentHelper.doPostDamageEffects(attacker, target);

        if (flag2) {
            attacker.crit(target);
        }

        return flag2 ? AttackResult.CRIT : AttackResult.HIT;
    }

    static void playMeleeSound(Player entity, ResourceLocation id, String key, float volume, float pitch) {
        playMeleeSound(entity, id, key, volume, pitch, false);
    }

    static void playMeleeSound(Player entity, ResourceLocation id, String key, float volume, float pitch, boolean exceptSelf) {
        var packet = new SCustomSound(SCustomSound.SoundType.MELEE, id, key, entity.position(), volume, pitch);
        ServerPlayer p;
        if (exceptSelf && entity instanceof ServerPlayer player) {
            p = player;
        } else {
            p = null;
        }
        NetworkHandler.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                p, entity.getX(), entity.getY(), entity.getZ(), 64, entity.level().dimension()
        )), packet);
    }

    default boolean canSprintingAttack() {
        return true;
    }

    @Override
    default boolean isSame(ItemStack i, ItemStack j) {
        IMeleeWeapon w1 = IMeleeWeapon.of(i);
        IMeleeWeapon w2 = IMeleeWeapon.of(j);
        if (w1 != null && w2 != null) {
            return w1.getId(i).equals(w2.getId(j));
        }
        if (i.isEmpty() || j.isEmpty()) {
            return i.isEmpty() && j.isEmpty();
        }
        return false;
    }
}
