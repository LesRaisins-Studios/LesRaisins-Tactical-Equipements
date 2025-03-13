package me.xjqsh.lrtactical.client.resource.display;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;

import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import me.xjqsh.lrtactical.api.animation.ThrowableAnimationStateContext;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.tacz.guns.client.model.GunModelConstant.LEFTHAND_POS_NODE;
import static com.tacz.guns.client.model.GunModelConstant.RIGHTHAND_POS_NODE;

public class ThrowableDisplayInstance {
    private ResourceLocation id;
    private BedrockAnimatedModel model;
    private LuaAnimationStateMachine<ThrowableAnimationStateContext> stateMachine;
    private ResourceLocation texture;
    private ItemTransforms transforms;

    private ThrowableDisplayInstance(){}

    public ResourceLocation getId() {
        return id;
    }

    public BedrockAnimatedModel getModel() {
        return model;
    }

    public LuaAnimationStateMachine<ThrowableAnimationStateContext> getStateMachine() {
        return stateMachine;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public ItemTransforms getTransforms() {
        return transforms;
    }

    @NotNull
    public static ThrowableDisplayInstance create(ThrowableDisplay pojo, ResourceLocation id) throws IllegalArgumentException {
        ThrowableDisplayInstance display = new ThrowableDisplayInstance();
        display.id = id;

        Preconditions.checkArgument(pojo.modelLocation != null, "display object missing model field");
        Preconditions.checkArgument(pojo.stateMachineLocation != null, "display object missing stateMachine field");
        Preconditions.checkArgument(pojo.textureLocation != null, "display object missing texture field");
        Preconditions.checkArgument(pojo.animationLocation != null, "display object missing animation field");

        BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(pojo.modelLocation);
        Preconditions.checkArgument(modelPOJO != null, "no corresponding model found for " + pojo.modelLocation);

        if (BedrockVersion.isLegacyVersion(modelPOJO)) {
            display.model = new BedrockAnimatedModel(modelPOJO, BedrockVersion.LEGACY);
        }
        display.model = new BedrockAnimatedModel(modelPOJO, BedrockVersion.NEW);
        // 左手手臂
        display.model.setFunctionalRenderer(LEFTHAND_POS_NODE, bedrockPart -> new LeftHandRender(display.model));
        // 右手手臂
        display.model.setFunctionalRenderer(RIGHTHAND_POS_NODE, bedrockPart -> new RightHandRender(display.model));

        var animation = ClientAssetsManager.INSTANCE.getBedrockAnimations(pojo.animationLocation);
        Preconditions.checkArgument(animation != null, "no corresponding model found for " + pojo.modelLocation);
        AnimationController controller = Animations.createControllerFromBedrock(animation, display.model);

        var script = ClientAssetsManager.INSTANCE.getScript(pojo.stateMachineLocation);
        Preconditions.checkArgument(script != null, "no corresponding model found for " + pojo.modelLocation);

        display.stateMachine = new LuaStateMachineFactory<ThrowableAnimationStateContext>()
                .setController(controller)
                .setLuaScripts(script)
                .build();
        display.texture = new ResourceLocation(pojo.textureLocation.getNamespace(), "textures/" + pojo.textureLocation.getPath() + ".png");

        display.transforms = pojo.transforms == null ? ItemTransforms.NO_TRANSFORMS : pojo.transforms;

        return display;
    }

    public record ThrowableDisplay(
            @SerializedName("model")
            ResourceLocation modelLocation,
            @SerializedName("animation")
            ResourceLocation animationLocation,
            @SerializedName("state_machine")
            ResourceLocation stateMachineLocation,
            @SerializedName("texture")
            ResourceLocation textureLocation,
            @SerializedName("transforms")
            ItemTransforms transforms
    ) {}
}
