package me.xjqsh.lrtactical.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.api.collision.ConeFilter;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import me.xjqsh.lrtactical.client.renderer.item.FlashShieldItemRenderer;
import me.xjqsh.lrtactical.init.ModEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

public class ShieldItem extends Item implements IMeleeWeapon, IAnimationItem {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ShieldItem() {
        super(new Properties().stacksTo(1).durability(200));
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
        return super.getMaxDamage(stack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private FlashShieldItemRenderer renderer = null;

            @Override
            public FlashShieldItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    renderer = new FlashShieldItemRenderer();
                    renderer.init();
                }
                return renderer;
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
        return action == MeleeAction.LEFT;
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
        boolean coolDown = player.getCapability(CombatPropertiesProvider.CAPABILITY).map(cap -> cap.getCoolDownTick() > 0).orElse(false);
        if (coolDown) {
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
                    player.getCooldowns().addCooldown(stack.getItem(), 20);
                }
                entity.addEffect(new MobEffectInstance(ModEffects.BLIND.get(), 45, 0, false, false));
                entity.addEffect(new MobEffectInstance(ModEffects.DEAFENED.get(), 60, 0, false, false));
            }
        }
        return stack;
    }

    @Override
    public boolean useOnRelease(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItem(stack1, stack2);
    }

    @OnlyIn(Dist.CLIENT)
    public void triggerAnimation(ItemStack stack, String animationName) {
        if (IClientItemExtensions.of(stack.getItem()).getCustomRenderer() instanceof AnimateGeoItemRenderer<?, ?> renderer) {
            renderer.triggerAnimation(stack, animationName);
        }
    }
}
