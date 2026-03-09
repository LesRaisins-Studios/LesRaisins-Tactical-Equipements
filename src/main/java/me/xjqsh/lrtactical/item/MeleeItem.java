package me.xjqsh.lrtactical.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.tacz.guns.api.item.IAnimationItem;
import me.xjqsh.lrtactical.api.collision.ITargetFilter;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.client.renderer.item.MeleeItemRenderer;
import me.xjqsh.lrtactical.config.CommonConfig;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.melee.CombatData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MeleeItem extends Item implements IAnimationItem, IMeleeWeapon {
    public MeleeItem() {
        super(new Properties().stacksTo(1).setNoRepair());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            return getMeleeIndex(stack).map(MeleeWeaponIndex::getDefaultModifiers).orElse(ImmutableMultimap.of());
        }
        return ImmutableMultimap.of();
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return IMeleeWeapon.super.isSame(stack1, stack2);
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

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack stack) {
        return this.getMeleeIndex(stack).map(MeleeWeaponIndex::getDescriptionId).orElse(super.getDescriptionId(stack));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        return super.getTooltipImage(pStack);
    }

    @Override
    public int getAttackCoolDown(ItemStack stack, MeleeAction action, int cnt) {
        return this.getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action, cnt))
                .map(CombatData.MeleeAttackInfo::getCooldown)
                .orElse(0);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return this.getMeleeIndex(stack).map(MeleeWeaponIndex::getMaxDurability).orElse(0);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return this.getMaxDamage(stack) > 0;
    }

    @Override
    public int getDrawTime(ItemStack stack) {
        return getMeleeIndex(stack).map(index -> index.getData().getDrawTime()).orElse(0);
    }

    @Override
    public int getPutAwayTime(ItemStack stack) {
        return getMeleeIndex(stack).map(index -> index.getData().getPutAwayTime()).orElse(0);
    }

    @Override
    public int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action, int cnt) {
        return getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action, cnt))
                .map(CombatData.MeleeAttackInfo::getDelay)
                .orElse(0);
    }

    @Override
    public CombatData.MeleeMovement getAttackMovement(Player entity, ItemStack stack, MeleeAction action) {
        return getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action))
                .map(CombatData.MeleeAttackInfo::getMovement)
                .orElse(null);
    }

    @Override
    public List<Entity> collectTargets(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction) {
        List<Entity> entities = new ArrayList<>();
        this.getMeleeIndex(stack).ifPresent(index -> {
            CombatData combatData = index.getData().getAttackInfo();
            if (combatData == null) {
                return;
            }
            var attackInfo = combatData.getAttackInfo(action);
            if (attackInfo == null) {
                return;
            }
            ITargetFilter filter = attackInfo.getHitbox();
            entities.addAll(filter.filterTargets(attacker, origin, direction));
        });
        return entities;
    }

    @Override
    public void attack(Player attacker, ItemStack stack, MeleeAction action, List<Entity> targets, int combo) {
        float base = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        this.getMeleeIndex(stack).ifPresent(index -> {
            CombatData combatData = index.getData().getAttackInfo();
            if (combatData == null) {
                return;
            }
            var attackInfo = combatData.getAttackInfo(action, combo);
            if (attackInfo == null) {
                return;
            }
            ITargetFilter filter = attackInfo.getHitbox();
            IMeleeWeapon.playMeleeSound(attacker, index.getId(), action.getId(), 2, 1, true);

            float damage = base * attackInfo.getFactor();
            float knockback = attackInfo.getKnockback();

            if (damage <= 0) return;
            boolean hit = false;
            boolean crit = false;
            boolean kill = false;
            for (Entity livingentity : targets) {
                boolean flag = !(livingentity instanceof ArmorStand armorStand) || !armorStand.isMarker();
                boolean inRange = livingentity.distanceToSqr(attacker) <= filter.getMaxRange() * filter.getMaxRange();

                if (livingentity != attacker && flag && inRange) {
                    var result = this.performAttack(attacker, livingentity, stack, damage, knockback);
                    hit |= result.hit();
                    crit |= result.crit();
                    kill |= result.kill();
                }
            }

            if (hit) {
                if (CommonConfig.MELEE_ITEM_CONSUME_DURABILITY.get()) {
                    stack.hurtAndBreak(attackInfo.getDurabilityDamage(), attacker, (player) -> {
                        player.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                    });
                }
                String soundKey;
                if (kill) {
                    soundKey = "kill";
                } else if (crit) {
                    soundKey = "crit";
                } else {
                    soundKey = action.getId() + "_hit";
                }
                IMeleeWeapon.playMeleeSound(attacker, index.getId(), soundKey, 2, 1);
            }
        });
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
