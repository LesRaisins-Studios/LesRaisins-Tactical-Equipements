package me.xjqsh.lrtactical.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class ThrowableEntityRenderer extends EntityRenderer<ThrowableItemEntity> {
    public ThrowableEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);

    }

    @Override
    public void render(ThrowableItemEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int light) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        poseStack.translate(0.1, 0.3, 0);

        if (entityIn.getItem() != null) {
            Minecraft.getInstance().getItemRenderer().renderStatic(entityIn.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferIn, entityIn.level(), 0);
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ThrowableItemEntity pEntity) {
        return null;
    }

}
