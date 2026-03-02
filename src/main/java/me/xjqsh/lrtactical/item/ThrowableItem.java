package me.xjqsh.lrtactical.item;

import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.capability.CustomItemCoolDownsProvider;
import me.xjqsh.lrtactical.client.renderer.item.ThrowableItemRendererWrapper;
import me.xjqsh.lrtactical.init.ModItems;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import me.xjqsh.lrtactical.item.throwable.area.EffectCloudThrowableData;
import me.xjqsh.lrtactical.item.throwable.explode.ExplodeThrowableData;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SThrowableAnimationSync;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

public class ThrowableItem extends Item implements IAnimationItem, IThrowable {
    public ThrowableItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return this.getThrowableIndex(stack).map(ThrowableIndex::getMaxStackSize).orElse(1);
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ThrowableItemRendererWrapper renderer = null;

            @Override
            public ThrowableItemRendererWrapper getCustomRenderer() {
                if (this.renderer == null) {
                    renderer = new ThrowableItemRendererWrapper();
                }
                return renderer;
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void triggerAnimation(ItemStack stack, String animationName) {
        if (IClientItemExtensions.of(stack.getItem()).getCustomRenderer() instanceof AnimateGeoItemRenderer<?, ?> renderer) {
            renderer.triggerAnimation(stack, animationName);
        }
    }

    @ParametersAreNonnullByDefault
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.fail(player.getItemInHand(pUsedHand));
        }
        ItemStack stack = player.getItemInHand(pUsedHand);
        boolean flag = getThrowableIndex(stack)
                .map(index -> index.getData().getCooldownCategory())
                .map(id -> player.getCapability(CustomItemCoolDownsProvider.CAPABILITY)
                        .map(cap -> cap.isOnCooldown(id))
                        .orElse(false)
                ).orElse(false);
        if (!flag) {
            player.startUsingItem(pUsedHand);

            // Sync PREPARE animation to tracking players
            if (!pLevel.isClientSide()) {
                var animationId = this.getId(stack);
                NetworkHandler.sendToTrackingEntity(
                        new SThrowableAnimationSync(player.getId(), SThrowableAnimationSync.ThrowableState.PREPARE, animationId),
                        player
                );
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    public void onThrow(Level world, LivingEntity entity, ItemStack stack, ThrowableIndex<?, ?> index) {
        var throwable = index.createEntity(stack, entity);
        if (index.getData().isCookable()) {
            int newLife = throwable.getLife() - (entity.getTicksUsingItem() - index.getData().getPrepareTime());
            newLife = Math.max(newLife, 0);
            throwable.setLife(newLife);
        }
        world.addFreshEntity(throwable);

        ResourceLocation id = index.getData().getCooldownCategory();
        if (id != null) {
            entity.getCapability(CustomItemCoolDownsProvider.CAPABILITY).ifPresent(cap -> {
                cap.addCooldown(id, index.getData().getCooldown());
            });
        }
        stack.shrink(1);

        // Sync THROW animation to tracking players
        if (entity instanceof Player player) {
            var animationId = this.getId(stack);
            NetworkHandler.sendToTrackingEntity(
                    new SThrowableAnimationSync(player.getId(), SThrowableAnimationSync.ThrowableState.THROW, animationId),
                    player
            );
        }

        if (index.getData() instanceof ExplodeThrowableData explode && explode.getExplode().isRemoteDetonation()) {
            ItemStack detonatorStack = new ItemStack(ModItems.DETONATOR.get());
            if (detonatorStack.getItem() instanceof DetonatorItem detonatorItem) {
                detonatorItem.recordEntity(throwable, detonatorStack);
            }
            entity.setItemInHand(InteractionHand.MAIN_HAND, detonatorStack);
        }

    }


    @ParametersAreNonnullByDefault
    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int pRemainingUseDuration) {
        this.getThrowableIndex(stack).ifPresent(index ->{
            var data = index.getData();
            if (data.isCookable() && entity.getTicksUsingItem() >= data.getPrepareTime() + data.getEntityData().getLifeTime()) {
                if (!world.isClientSide()) {
                    onThrow(world, entity, stack, index);
                    entity.stopUsingItem();
                }
            }
        });
    }

    @ParametersAreNonnullByDefault
    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        this.getThrowableIndex(stack).ifPresent(index ->{
            if (entity.getTicksUsingItem() >= index.getData().getPrepareTime()) {
                if (!world.isClientSide()) {
                    onThrow(world, entity, stack, index);
                }
            }
        });
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        super.onStopUsing(stack, entity, count);
    }

    @Override
    public boolean useOnRelease(@NotNull ItemStack pStack) {
        return true;
    }

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack stack) {
        return this.getThrowableIndex(stack).map(ThrowableIndex::getDescriptionId).orElse(super.getDescriptionId(stack));
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return IThrowable.super.isSame(stack1, stack2);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        this.getThrowableIndex(stack).ifPresent(index -> {
            if (index.getData() instanceof EffectCloudThrowableData data) {
                PotionUtils.addPotionTooltip(data.getCloudData().getEffectInstances(), pTooltipComponents, 1.0F);
            }
        });
    }
}
