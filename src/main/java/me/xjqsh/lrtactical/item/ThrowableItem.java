package me.xjqsh.lrtactical.item;

import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.capability.CustomItemCoolDownsProvider;
import me.xjqsh.lrtactical.client.renderer.item.ThrowableItemRendererWrapper;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

public class ThrowableItem extends Item implements IAnimationItem, IThrowable {
    public ThrowableItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getThrowableIndex(stack).map(index -> index.getData().getStackSize()).orElse(1);
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
        }
        return InteractionResultHolder.consume(stack);
    }


    @ParametersAreNonnullByDefault
    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
    }

    @ParametersAreNonnullByDefault
    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        getThrowableIndex(stack).ifPresent(index ->{
            if (entity.getTicksUsingItem() >= index.getData().getPrepareTime()) {
                if (world.isClientSide()) {
                    this.triggerAnimation(stack, "throw");
                } else {
                    var throwable = index.createEntity(stack, entity);
                    world.addFreshEntity(throwable);
                    ResourceLocation id = index.getData().getCooldownCategory();
                    if (id != null) {
                        entity.getCapability(CustomItemCoolDownsProvider.CAPABILITY).ifPresent(cap -> {
                            cap.addCooldown(id, index.getData().getCooldown());
                        });
                    }
                    stack.shrink(1);
                }
            }
        });
    }

    @Override
    public boolean useOnRelease(@NotNull ItemStack pStack) {
        return true;
    }

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack stack) {
        return getThrowableIndex(stack).map(ThrowableIndex::getDescriptionId).orElse(super.getDescriptionId(stack));
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return IThrowable.super.isSame(stack1, stack2);
    }
}
