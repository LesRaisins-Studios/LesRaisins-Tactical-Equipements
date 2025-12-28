package me.xjqsh.lrtactical.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.xjqsh.lrtactical.client.renderer.item.ThrowableItemRendererWrapper;
import me.xjqsh.lrtactical.client.renderer.model.CustomBedrockModel;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

        poseStack.translate(0, 0.15, 0);
        float yRot = Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
        poseStack.mulPose(Axis.YN.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        poseStack.translate(0, 0.35, -0.15);

        if (entityIn.getItem() != null) {
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
