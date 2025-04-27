package me.xjqsh.lrtactical.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.animation.MeleeAnimationStateContext;
import me.xjqsh.lrtactical.client.resource.display.MeleeDisplayInstance;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.item.ItemDisplayContext.GUI;

public class MeleeItemRenderer extends AnimateGeoItemRenderer<BedrockAnimatedModel, MeleeAnimationStateContext> {
    private static final SlotModel SLOT_MODEL = new SlotModel();

    @Override
    public MeleeAnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
        var context = new MeleeAnimationStateContext();
        this.updateContext(context, stack, player, partialTick);
        return context;
    }

    @Override
    public void updateContext(MeleeAnimationStateContext context, ItemStack stack, Player player, float partialTick) {
        context.setPartialTicks(partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemStack stack) {
        return LrTacticalAPI.getMeleeDisplay(stack).map(MeleeDisplayInstance::getTexture).orElse(null);
    }

    @Override
    @Nullable
    public LuaAnimationStateMachine<MeleeAnimationStateContext> getStateMachine(ItemStack stack) {
        return LrTacticalAPI.getMeleeDisplay(stack).map(MeleeDisplayInstance::getStateMachine).orElse(null);
    }

    @Override
    public BedrockAnimatedModel getModel(ItemStack stack) {
        return LrTacticalAPI.getMeleeDisplay(stack).map(MeleeDisplayInstance::getModel).orElse(null);
    }

    @Override
    public long getPutAwayTime(ItemStack stack) {
        return LrTacticalAPI.getMeleeIndex(stack)
                .map(MeleeWeaponIndex::getData)
                .map(throwableData -> throwableData.getPutAwayTime() * 50L)
                .orElse(0L);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (ctx.firstPerson()) return;
        LrTacticalAPI.getMeleeDisplay(stack).ifPresent(display -> {
            BedrockAnimatedModel model = display.getModel();
            // GUI 特殊渲染
            if (ctx == GUI && display.getSlotTexture() != null) {
                poseStack.translate(0.5, 1.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(display.getSlotTexture()));
                SLOT_MODEL.renderToBuffer(poseStack, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                return;
            }
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
