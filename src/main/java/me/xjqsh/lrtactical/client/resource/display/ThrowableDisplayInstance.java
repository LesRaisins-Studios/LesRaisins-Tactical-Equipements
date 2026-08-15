package me.xjqsh.lrtactical.client.resource.display;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import me.xjqsh.lrtactical.api.animation.ThrowableAnimationStateContext;
import me.xjqsh.lrtactical.client.audio.ICustomSoundSupplier;
import me.xjqsh.lrtactical.client.renderer.model.CustomBedrockModel;
import me.xjqsh.lrtactical.compat.player_animator.ThirdPersonAnimationConfig;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Objects;

public class ThrowableDisplayInstance implements ICustomSoundSupplier {
    /**
     * 实体渲染时的额外变换默认值：绕 Z 轴旋转 90 度
     */
    public static final ItemTransform DEFAULT_ENTITY_TRANSFORM =
            new ItemTransform(new Vector3f(0, 0, 90), new Vector3f(-0.3f, 0.15f, 0), new Vector3f(1, 1, 1));

    private ResourceLocation id;
    private BedrockAnimatedModel model;
    private LuaAnimationStateMachine<ThrowableAnimationStateContext> stateMachine;
    private ResourceLocation texture;
    private ResourceLocation slotTexture;
    private ItemTransforms transforms;
    private Vector3f displayOffset;
    private ItemTransform entityTransform;
    private Map<String, ResourceLocation> sounds;
    private ThirdPersonAnimationConfig thirdPersonAnimation;

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

    public ResourceLocation getSlotTexture() {
        return slotTexture;
    }

    public ItemTransforms getTransforms() {
        return transforms;
    }

    public Vector3f getDisplayOffset() {
        return displayOffset;
    }

    public ItemTransform getEntityTransform() {
        return entityTransform;
    }

    @Override
    public Map<String, ResourceLocation> getSounds() {
        return sounds;
    }

    @Nullable
    public ThirdPersonAnimationConfig getThirdPersonAnimation() {
        return thirdPersonAnimation;
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
            display.model = new CustomBedrockModel(modelPOJO, BedrockVersion.LEGACY);
        }
        display.model = new CustomBedrockModel(modelPOJO, BedrockVersion.NEW);

        var animation = ClientAssetsManager.INSTANCE.getBedrockAnimations(pojo.animationLocation);
        Preconditions.checkArgument(animation != null, "no corresponding animation found for " + pojo.modelLocation);
        AnimationController controller = Animations.createControllerFromBedrock(animation, display.model);

        var script = ClientAssetsManager.INSTANCE.getScript(pojo.stateMachineLocation);
        Preconditions.checkArgument(script != null, "no corresponding state machine found for " + pojo.modelLocation);

        display.stateMachine = new LuaStateMachineFactory<ThrowableAnimationStateContext>()
                .setController(controller)
                .setLuaScripts(script)
                .build();
        display.texture = new ResourceLocation(pojo.textureLocation.getNamespace(), "textures/" + pojo.textureLocation.getPath() + ".png");
        if (pojo.slotTextureLocation != null) {
            display.slotTexture = new ResourceLocation(pojo.slotTextureLocation.getNamespace(), "textures/" + pojo.slotTextureLocation.getPath() + ".png");
        }

        display.transforms = Objects.requireNonNullElse(pojo.transforms, ItemTransforms.NO_TRANSFORMS);
        display.displayOffset = Objects.requireNonNullElse(pojo.displayOffset, new Vector3f());
        display.entityTransform = Objects.requireNonNullElse(pojo.entityTransform, DEFAULT_ENTITY_TRANSFORM);
        display.sounds = Objects.requireNonNullElseGet(pojo.sounds, Maps::newHashMap);
        display.thirdPersonAnimation = pojo.thridPersonAnimation;

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
            @SerializedName("slot_texture")
            ResourceLocation slotTextureLocation,
            @SerializedName("transforms")
            ItemTransforms transforms,
            @SerializedName("display_offset")
            Vector3f displayOffset,
            @SerializedName("entity_transform")
            ItemTransform entityTransform,
            @SerializedName("sounds")
            Map<String, ResourceLocation> sounds,
            @SerializedName("thrid_person_animation")
            ThirdPersonAnimationConfig thridPersonAnimation
    ) {}
}
