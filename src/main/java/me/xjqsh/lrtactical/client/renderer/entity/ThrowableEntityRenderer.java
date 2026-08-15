package me.xjqsh.lrtactical.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.client.renderer.item.ThrowableItemRendererWrapper;
import me.xjqsh.lrtactical.client.renderer.model.CustomBedrockModel;
import me.xjqsh.lrtactical.entity.StickyGrenadeEntity;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class ThrowableEntityRenderer extends EntityRenderer<ThrowableItemEntity> {
    public ThrowableEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);

    }

    @Override
    public void render(ThrowableItemEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int light) {
        poseStack.pushPose();

        if (entityIn.getItem() != null) {
            if (entityIn instanceof StickyGrenadeEntity stickyGrenade) {
                Direction stuckFace = stickyGrenade.getStuckFace();
                if (stuckFace != null) {
                    poseStack.translate(
                            -stuckFace.getStepX() * 0.15,
                            (1 - stuckFace.getStepY()) * 0.15,
                            -stuckFace.getStepZ() * 0.15
                    );
                }
            }

            float yRot = Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
            poseStack.mulPose(Axis.YN.rotationDegrees(yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));

            // 应用 display 中配置的实体额外变换（默认绕 Z 轴旋转 90 度）
            LrTacticalAPI.getThrowableDisplay(entityIn.getItem()).ifPresent(display -> {
                ItemTransform entityTransform = display.getEntityTransform();
                if (entityTransform != null) {
                    entityTransform.apply(false, poseStack);
                }
            });

            CustomBedrockModel model = null;
            if (IClientItemExtensions.of(entityIn.getItem()).getCustomRenderer() instanceof ThrowableItemRendererWrapper renderer) {
                var m = renderer.getModel(entityIn.getItem());
                if (m instanceof CustomBedrockModel customModel) {
                    model = customModel;
                    model.setEntityRendering(true);
                }
            }

            Minecraft.getInstance().getItemRenderer().renderStatic(entityIn.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferIn, entityIn.level(), 0);
            if (model != null) {
                model.setEntityRendering(false);
            }
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ThrowableItemEntity pEntity) {
        return null;
    }

}
