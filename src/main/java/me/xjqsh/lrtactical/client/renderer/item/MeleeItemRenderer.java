package me.xjqsh.lrtactical.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import me.xjqsh.lrtactical.EquipmentMod;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static com.tacz.guns.client.model.GunModelConstant.LEFTHAND_POS_NODE;
import static com.tacz.guns.client.model.GunModelConstant.RIGHTHAND_POS_NODE;

public class MeleeItemRenderer extends AnimateGeoItemRenderer<BedrockAnimatedModel, ItemAnimationStateContext> {
    public MeleeItemRenderer() {
        super();
        init();
    }

    @Override
    public ItemAnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
        var context = new ItemAnimationStateContext();
        this.updateContext(context, stack, player, partialTick);
        return context;
    }

    @Override
    public void updateContext(ItemAnimationStateContext context, ItemStack stack, Player player, float partialTick) {
        context.setPartialTicks(partialTick);
    }

    @Override
    public long getPutAwayTime(ItemStack stack) {
        return 400;
    }

    @Override
    public BedrockAnimatedModel getModel(ItemStack stack) {
        return super.getModel(stack);
    }

    @Override
    public void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
        super.renderFirstPerson(player, stack, ctx, poseStack, bufferSource, light, partialTick);
    }

    public void init() {
        var pojo = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(new ResourceLocation(EquipmentMod.MOD_ID, "melee/karambit_geo"));
        BedrockAnimatedModel model = new BedrockAnimatedModel(pojo, BedrockVersion.NEW);
        // 左手手臂
        model.setFunctionalRenderer(LEFTHAND_POS_NODE, bedrockPart -> new LeftHandRender(model));
        // 右手手臂
        model.setFunctionalRenderer(RIGHTHAND_POS_NODE, bedrockPart -> new RightHandRender(model));
        this.setModel(model);

        this.textureLocation = new ResourceLocation(EquipmentMod.MOD_ID, "textures/melee/karambit_uv.png");

        var animation = ClientAssetsManager.INSTANCE.getBedrockAnimations(new ResourceLocation(EquipmentMod.MOD_ID, "melee/karambit"));
        AnimationController controller = Animations.createControllerFromBedrock(animation, model);

        var script = ClientAssetsManager.INSTANCE.getScript(new ResourceLocation(EquipmentMod.MOD_ID, "default_melee_state_machine"));

        stateMachine = new LuaStateMachineFactory<ItemAnimationStateContext>()
                .setController(controller)
                .setLuaScripts(script)
                .build();
    }
}
