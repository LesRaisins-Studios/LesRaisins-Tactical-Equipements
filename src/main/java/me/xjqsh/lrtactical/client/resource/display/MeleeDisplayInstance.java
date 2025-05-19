package me.xjqsh.lrtactical.client.resource.display;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
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
import me.xjqsh.lrtactical.api.animation.BaseAnimationStateContext;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

import static com.tacz.guns.client.model.GunModelConstant.LEFTHAND_POS_NODE;
import static com.tacz.guns.client.model.GunModelConstant.RIGHTHAND_POS_NODE;

public class MeleeDisplayInstance {
    private ResourceLocation id;
    private BedrockAnimatedModel model;
    private LuaAnimationStateMachine<BaseAnimationStateContext> stateMachine;
    private ResourceLocation texture;
    private ResourceLocation slotTexture;
    private ItemTransforms transforms;
    private Map<String, ResourceLocation> sounds;

    private MeleeDisplayInstance(){}

    public ResourceLocation getId() {
        return id;
    }

    public BedrockAnimatedModel getModel() {
        return model;
    }

    public LuaAnimationStateMachine<BaseAnimationStateContext> getStateMachine() {
        return stateMachine;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public ResourceLocation getSlotTexture() {
        return slotTexture;
    }

    public ItemTransforms getTransforms() {
        return transforms;
    }

    public Map<String, ResourceLocation> getSounds() {
        return sounds;
    }

    @NotNull
    public static MeleeDisplayInstance create(MeleeDisplay pojo, ResourceLocation id) throws IllegalArgumentException {
        MeleeDisplayInstance display = new MeleeDisplayInstance();
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

        display.stateMachine = new LuaStateMachineFactory<BaseAnimationStateContext>()
                .setController(controller)
                .setLuaScripts(script)
                .build();
        display.texture = new ResourceLocation(pojo.textureLocation.getNamespace(), "textures/" + pojo.textureLocation.getPath() + ".png");

        if (pojo.slotTextureLocation != null) {
            display.slotTexture = new ResourceLocation(pojo.slotTextureLocation.getNamespace(), "textures/" + pojo.slotTextureLocation.getPath() + ".png");
        }

        display.transforms = Objects.requireNonNullElse(pojo.transforms, ItemTransforms.NO_TRANSFORMS);
        display.sounds = Objects.requireNonNullElseGet(pojo.sounds, Maps::newHashMap);

        return display;
    }

    public record MeleeDisplay(
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
            ItemTransforms transforms,
            @SerializedName("sounds")
            Map<String, ResourceLocation> sounds
    ) {}
}
