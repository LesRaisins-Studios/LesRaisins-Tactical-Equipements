package me.xjqsh.lrtactical.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.animation.BaseAnimationStateContext;
import me.xjqsh.lrtactical.client.renderer.JumpSwayUtil;
import me.xjqsh.lrtactical.client.renderer.model.CustomBedrockModel;
import me.xjqsh.lrtactical.client.resource.display.MeleeDisplayInstance;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.item.ItemDisplayContext.GUI;

public class MeleeItemRenderer extends AnimateGeoItemRenderer<CustomBedrockModel, BaseAnimationStateContext> {
    private static final SlotModel SLOT_MODEL = new SlotModel();

    @Override
    public BaseAnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
        var context = new BaseAnimationStateContext();
        this.updateContext(context, stack, player, partialTick);
        return context;
    }

    @Override
    public void updateContext(BaseAnimationStateContext context, ItemStack stack, Player player, float partialTick) {
        context.setPartialTicks(partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemStack stack) {
        return LrTacticalAPI.getMeleeDisplay(stack).map(MeleeDisplayInstance::getTexture).orElse(null);
    }

    @Override
    @Nullable
    public LuaAnimationStateMachine<BaseAnimationStateContext> getStateMachine(ItemStack stack) {
        return LrTacticalAPI.getMeleeDisplay(stack).map(MeleeDisplayInstance::getStateMachine).orElse(null);
    }

    @Override
    public CustomBedrockModel getModel(ItemStack stack) {
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
    public void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource,
                                  int light, float partialTick) {
        var model = getModel(stack);
        if (model != null) {
            poseStack.pushPose();
            var stateMachine = getStateMachine(stack);
            if (stateMachine != null) {
                stateMachine.processContextIfExist(context -> {
                    updateContext(context, stack, player, partialTick);
                });
                stateMachine.update();
            }

            float xRotOffset = Mth.lerp(partialTick, player.xBobO, player.xBob);
            float yRotOffset = Mth.lerp(partialTick, player.yBobO, player.yBob);
            float xRot = player.getViewXRot(partialTick) - xRotOffset;
            float yRot = player.getViewYRot(partialTick) - yRotOffset;
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot * -0.1F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot * -0.1F));
            BedrockPart rootNode = model.getRootNode();
            if (rootNode != null) {
                xRot = (float) Math.tanh(xRot / 25) * 25;
                yRot = (float) Math.tanh(yRot / 25) * 25;
                rootNode.offsetX += yRot * 0.1F / 16F / 3F;
                rootNode.offsetY += -xRot * 0.1F / 16F / 3F;
                rootNode.additionalQuaternion.mul(Axis.XP.rotationDegrees(xRot * 0.05F));
                rootNode.additionalQuaternion.mul(Axis.YP.rotationDegrees(yRot * 0.05F));
            }

            // 从渲染原点 (0, 24, 0) 移动到模型原点 (0, 0, 0)
            poseStack.translate(0, 1.5f, 0);
            // 基岩版模型是上下颠倒的，需要翻转过来。
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            doExtraTransforms(poseStack, model, stack);

            model.setEffectVisible(true);
            model.render(poseStack, ctx, getRenderType(stack), light, OverlayTexture.NO_OVERLAY);
            model.setEffectVisible(false);

            // 渲染结束后清除动画变换
            model.cleanAnimationTransform();
            poseStack.popPose();
        }
    }

    @Override
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, float multiplier) {
        super.applyItemInHandCameraAnimation(event, stack, multiplier);
        // 截至目前，摄像机动画数据已消费完毕。是否有更好的清理动画数据的方法？
        var model = this.getModel(stack);
        if (model != null) {
            model.cleanCameraAnimationTransform();
        }
    }

    @Override
    public void doExtraTransforms(PoseStack poseStack, CustomBedrockModel model, ItemStack stack) {
        super.doExtraTransforms(poseStack, model, stack);
        JumpSwayUtil.applyJumpingSway(model, Minecraft.getInstance().getFrameTime());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (ctx.firstPerson()) return;
        LrTacticalAPI.getMeleeDisplay(stack).ifPresentOrElse(display -> {
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
        }, () -> {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            SLOT_MODEL.renderToBuffer(poseStack, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        });
    }
}
