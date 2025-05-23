package me.xjqsh.lrtactical.client.renderer.item;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.animation.FlashShieldAnimationStateContext;
import me.xjqsh.lrtactical.client.resource.LrClientAssetsManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.InputStreamReader;

import static com.tacz.guns.client.model.GunModelConstant.LEFTHAND_POS_NODE;
import static com.tacz.guns.client.model.GunModelConstant.RIGHTHAND_POS_NODE;
import static net.minecraft.world.item.ItemDisplayContext.GUI;
import static net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

public class FlashShieldItemRenderer extends AnimateGeoItemRenderer<BedrockAnimatedModel, FlashShieldAnimationStateContext> {
    private static final SlotModel SLOT_MODEL = new SlotModel();
    private ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;
    private ResourceLocation slotTexture = null;

    @Override
    public FlashShieldAnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
        var context = new FlashShieldAnimationStateContext();
        this.updateContext(context, stack, player, partialTick);
        return context;
    }

    @Override
    public void updateContext(FlashShieldAnimationStateContext context, ItemStack stack, Player player, float partialTick) {
        context.setPartialTicks(partialTick);
        context.setUsing(player.isUsingItem());
        context.setUsingTick(player.getTicksUsingItem());
        context.setPartialTicks(partialTick);
        context.setCurrentItem(stack);
    }

    @Override
    public long getPutAwayTime(ItemStack stack) {
        return 320;
    }

    @Override
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, float multiplier) {
        super.applyItemInHandCameraAnimation(event, stack, multiplier);
        // 截至目前，摄像机动画数据已消费完毕。是否有更好的清理动画数据的方法？
        model.cleanCameraAnimationTransform();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (ctx.firstPerson()) return;
        if (ctx == THIRD_PERSON_LEFT_HAND) return;
        // GUI 特殊渲染
        if (ctx == GUI && slotTexture != null) {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(slotTexture));
            SLOT_MODEL.renderToBuffer(poseStack, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }
        poseStack.pushPose();
        {
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
    }

    public void init(ShieldDisplay display) {
        BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE
                .getBedrockModelPOJO(display.modelLocation);
        model = new BedrockAnimatedModel(modelPOJO, BedrockVersion.NEW);
        // 左手手臂
        model.setFunctionalRenderer(LEFTHAND_POS_NODE, bedrockPart -> new LeftHandRender(model));
        // 右手手臂
        model.setFunctionalRenderer(RIGHTHAND_POS_NODE, bedrockPart -> new RightHandRender(model));

        textureLocation = new ResourceLocation(display.textureLocation.getNamespace(), "textures/" + display.textureLocation.getPath() + ".png");

        this.transforms = display.transforms == null ? ItemTransforms.NO_TRANSFORMS : display.transforms;

        var animation = ClientAssetsManager.INSTANCE.getBedrockAnimations(display.animationLocation);
        AnimationController controller = null;
        if (animation != null) {
            controller = Animations.createControllerFromBedrock(animation, model);
        }

        var script = ClientAssetsManager.INSTANCE.getScript(display.stateMachineLocation);
        stateMachine = new LuaStateMachineFactory<FlashShieldAnimationStateContext>()
                .setController(controller)
                .setLuaScripts(script)
                .build();
    }

    public void onResourceManagerReload(ResourceManager manager) {
        manager.getResource(new ResourceLocation(EquipmentMod.MOD_ID, "display/shield/flash_shield.json"))
                .map(resource -> {
                    try (var stream = resource.open()) {
                        return LrClientAssetsManager.GSON.fromJson(new InputStreamReader(stream), ShieldDisplay.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .ifPresent(this::init);
    }


    public record ShieldDisplay(
            @SerializedName("model")
            ResourceLocation modelLocation,
            @SerializedName("animation")
            ResourceLocation animationLocation,
            @SerializedName("state_machine")
            ResourceLocation stateMachineLocation,
            @SerializedName("texture")
            ResourceLocation textureLocation,
            @SerializedName("slot_texture")
            ResourceLocation slotTextureLocation,
            @SerializedName("transforms")
            ItemTransforms transforms
    ) {}
}
