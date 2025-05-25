package me.xjqsh.lrtactical.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.tacz.guns.api.item.IAnimationItem;
import me.xjqsh.lrtactical.api.collision.ITargetFilter;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.client.renderer.item.MeleeItemRenderer;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.melee.CombatData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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

import java.util.Optional;
import java.util.function.Consumer;

public class MeleeItem extends Item implements IAnimationItem, IMeleeWeapon {
    public MeleeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            return getMeleeIndex(stack).map(MeleeWeaponIndex::getDefaultModifiers).orElse(ImmutableMultimap.of());
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

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack stack) {
        return getMeleeIndex(stack).map(MeleeWeaponIndex::getDescriptionId).orElse(super.getDescriptionId(stack));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        return super.getTooltipImage(pStack);
    }

    @Override
    public int getAttackCoolDown(ItemStack stack, MeleeAction action) {
        return getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action))
                .map(CombatData.MeleeAttackInfo::cooldown)
                .orElse(0);
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
    public int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action) {
        return getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action))
                .map(CombatData.MeleeAttackInfo::delay)
                .orElse(0);
    }

    @Override
    public void attack(Player attacker, ItemStack stack, MeleeAction action, final Vec3 origin, final Vec3 direction) {
        float base = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        getMeleeIndex(stack)
                .map(index -> index.getData().getAttackInfo())
                .map(attackInfos -> attackInfos.getAttackInfo(action))
                .ifPresent(attackInfo -> {
                    float damage = base * attackInfo.factor();
                    float knockback = attackInfo.knockback();
                    ITargetFilter filter = attackInfo.hitbox();
                    Vec3 origin1 = new Vec3(origin.x, origin.y, origin.z);
                    Vec3 direction1 = new Vec3(direction.x, direction.y, direction.z);

                    if (origin1.distanceToSqr(attacker.getEyePosition()) > attacker.getDeltaMovement().lengthSqr() * 4) {
                        origin1 = attacker.getEyePosition();
                        direction1 = attacker.getLookAngle();
                    }

                    SoundEvent soundEvent = switch (action) {
                        case LEFT -> SoundEvents.PLAYER_ATTACK_WEAK;
                        case RIGHT -> SoundEvents.PLAYER_ATTACK_STRONG;
                    };

//                    attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
//                            soundEvent, attacker.getSoundSource(), 1.0F, 1.0F);

                    if (damage <= 0) return;
                    for (Entity livingentity : filter.filterTargets(attacker, origin1, direction1)) {
                        boolean flag = !(livingentity instanceof ArmorStand armorStand) || !armorStand.isMarker();

                        if (livingentity != attacker && !attacker.isAlliedTo(livingentity) && flag) {
                            this.performAttack(attacker, livingentity, stack, damage, knockback);
                        }
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
