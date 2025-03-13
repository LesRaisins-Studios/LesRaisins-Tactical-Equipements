package me.xjqsh.lrtactical.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.client.resource.display.ThrowableDisplayInstance;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import me.xjqsh.lrtactical.api.animation.ThrowableAnimationStateContext;
import org.jetbrains.annotations.Nullable;

/**
 * 投掷物渲染
 */
public class ThrowableItemRendererWrapper extends AnimateGeoItemRenderer<BedrockAnimatedModel, ThrowableAnimationStateContext> {
    private static final SlotModel SLOT_MODEL = new SlotModel();
    @Override
    public ThrowableAnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
        ThrowableAnimationStateContext context = new ThrowableAnimationStateContext();
        this.updateContext(context, stack, player, partialTick);
        return context;
    }

    @Override
    public void updateContext(ThrowableAnimationStateContext context, ItemStack stack, Player player, float partialTick) {
        context.setUsing(player.isUsingItem());
        context.setUsingTick(player.getTicksUsingItem());
        context.setPartialTicks(partialTick);
        context.setCurrentItem(stack);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemStack stack) {
        return LrTacticalAPI.getThrowableDisplay(stack).map(ThrowableDisplayInstance::getTexture).orElse(null);
    }

    @Override
    @Nullable
    public LuaAnimationStateMachine<ThrowableAnimationStateContext> getStateMachine(ItemStack stack) {
        return LrTacticalAPI.getThrowableDisplay(stack).map(ThrowableDisplayInstance::getStateMachine).orElse(null);
    }

    @Override
    public BedrockAnimatedModel getModel(ItemStack stack) {
        return LrTacticalAPI.getThrowableDisplay(stack).map(ThrowableDisplayInstance::getModel).orElse(null);
    }

    @Override
    public long getPutAwayTime(ItemStack stack) {
        return LrTacticalAPI.getThrowableIndex(stack)
                .map(ThrowableIndex::getData)
                .map(ThrowableData::getPutAwayTime)
                .orElse(0L);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (ctx.firstPerson()) return;
        LrTacticalAPI.getThrowableDisplay(stack).ifPresent(display -> {
            BedrockAnimatedModel model = display.getModel();
            poseStack.pushPose();
            {
                ItemTransforms transforms = display.getTransforms();
                if (transforms != null) {
                    poseStack.translate(0.5F, 0.5F, 0.5F);
                    ItemTransform transform = transforms.getTransform(ctx);
                    transform.apply(false, poseStack);
                    poseStack.translate(-0.5F, -0.5F, -0.5F);
                }

                // 从渲染原点 (0, 24, 0) 移动到模型原点 (0, 0, 0)
                poseStack.translate(0.5, 1.5f, 0.5);
                // 基岩版模型是上下颠倒的，需要翻转过来。
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
                if (model != null) {
                    model.render(poseStack, ctx, RenderType.entityCutout(
                            getTextureLocation(stack)
                    ), light, overlay);
                } else {
                    VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
                    SLOT_MODEL.renderToBuffer(poseStack, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
            poseStack.popPose();
        });
    }
}
