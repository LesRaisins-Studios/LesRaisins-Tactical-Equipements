package me.xjqsh.lrtactical.client.renderer.item;

import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.tacz.guns.client.model.GunModelConstant.LEFTHAND_POS_NODE;
import static com.tacz.guns.client.model.GunModelConstant.RIGHTHAND_POS_NODE;

public class FlashShieldItemRenderer extends AnimateGeoItemRenderer<BedrockAnimatedModel, FlashShieldAnimationStateContext> {
    private static final SlotModel SLOT_MODEL = new SlotModel();

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

    public void init() {
        BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE
                .getBedrockModelPOJO(new ResourceLocation(EquipmentMod.MOD_ID, "shield/flash_shield_geo"));
        model = new BedrockAnimatedModel(modelPOJO, BedrockVersion.NEW);
        // 左手手臂
        model.setFunctionalRenderer(LEFTHAND_POS_NODE, bedrockPart -> new LeftHandRender(model));
        // 右手手臂
        model.setFunctionalRenderer(RIGHTHAND_POS_NODE, bedrockPart -> new RightHandRender(model));

        var animation = ClientAssetsManager.INSTANCE.getBedrockAnimations(new ResourceLocation(EquipmentMod.MOD_ID, "shield/flash_shield"));
        AnimationController controller = Animations.createControllerFromBedrock(animation, model);

        var script = ClientAssetsManager.INSTANCE.getScript(new ResourceLocation(EquipmentMod.MOD_ID, "flash_shield_state_machine"));

        textureLocation = new ResourceLocation(EquipmentMod.MOD_ID, "textures/shield/flash_shield_uv.png");

        stateMachine = new LuaStateMachineFactory<FlashShieldAnimationStateContext>()
                .setController(controller)
                .setLuaScripts(script)
                .build();
    }

}
