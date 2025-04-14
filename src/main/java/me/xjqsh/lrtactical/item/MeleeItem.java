package me.xjqsh.lrtactical.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.tacz.guns.api.item.IAnimationItem;
import me.xjqsh.lrtactical.api.collision.ConeFilter;
import me.xjqsh.lrtactical.api.collision.ITargetFilter;
import me.xjqsh.lrtactical.api.collision.RayFilter;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.client.renderer.item.MeleeItemRenderer;
import me.xjqsh.lrtactical.util.VectorUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MeleeItem extends Item implements IAnimationItem, IMeleeWeapon {
    public MeleeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 12, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return ImmutableMultimap.of();
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return ItemStack.matches(stack1, stack2);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MeleeItemRenderer renderer = null;

            @Override
            public MeleeItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    renderer = new MeleeItemRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 5;
    }

    @Override
    public int getAttackCoolDown(ItemStack stack, MeleeAction action) {
        return switch (action) {
            case LEFT -> 10;
            case RIGHT -> 18;
        };
    }

    @Override
    public int getDrawTime(ItemStack stack) {
        return 15;
    }

    @Override
    public int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action) {
        return switch (action) {
            case LEFT -> 0;
            case RIGHT -> 5;
        };
    }

    @Override
    public void attack(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction) {
        float base = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ITargetFilter filter = switch (action) {
            case LEFT -> new ConeFilter(3d, 90d);
            case RIGHT -> new RayFilter(3.5d, 1);
        };

        if (origin.distanceToSqr(attacker.getEyePosition()) > attacker.getDeltaMovement().lengthSqr() * 4) {
            origin = attacker.getEyePosition();
            direction = attacker.getLookAngle();
        }

        SoundEvent soundEvent = switch (action) {
            case LEFT -> SoundEvents.PLAYER_ATTACK_WEAK;
            case RIGHT -> SoundEvents.PLAYER_ATTACK_STRONG;
        };
        attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                soundEvent, attacker.getSoundSource(), 1.0F, 1.0F);

        for (Entity livingentity : filter.filterTargets(attacker, origin, direction)) {
            boolean flag = !(livingentity instanceof ArmorStand armorStand) || !armorStand.isMarker();

            if (livingentity != attacker && !attacker.isAlliedTo(livingentity) && flag) {
                this.performAttack(attacker, livingentity, stack, base);
            }
        }
    }

    public void performAttack(Player attacker, Entity target, ItemStack stack, float base) {
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
            living.knockback(0.4F, Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F)));
        }

        boolean flag2 = attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable() && !attacker.isInWater()
                && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && target instanceof LivingEntity;

        // 原版跳劈暴击
        CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(attacker, target, flag2, flag2 ? 1.5F : 1.0F);
        if (hitResult != null) {
            base *= hitResult.getDamageModifier();
        }

        int j = EnchantmentHelper.getFireAspect(attacker);
        if (target instanceof LivingEntity living) {
            if (j > 0) {
                living.setSecondsOnFire(j * 4);
            }
        }

        target.invulnerableTime = 0;
        target.hurt(attacker.damageSources().playerAttack(attacker), base + modifier);

        if (flag2) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
            attacker.crit(target);
        }
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.WEAPON;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == ToolActions.SWORD_SWEEP;
    }
}
