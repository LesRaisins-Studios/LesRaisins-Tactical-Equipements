package me.xjqsh.lrtactical.compat.player_animator;

import com.tacz.guns.GunMod;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.util.IPAAssetManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

public class PlayerAnimatorIntegration {
    public static final ResourceLocation UPPER_LAYER = new ResourceLocation(EquipmentMod.MOD_ID, "upper");
    public static final ResourceLocation LOWER_LAYER = new ResourceLocation(EquipmentMod.MOD_ID, "lower");
    public static final ResourceLocation ROTATION_ANIMATION = new ResourceLocation(GunMod.MOD_ID, "rotation");
    private static boolean initialized = false;

    public enum AnimationLayer {
        UPPER(UPPER_LAYER, 40),
        LOWER(LOWER_LAYER, 41),
        ROTATION(ROTATION_ANIMATION, 42);

        private final ResourceLocation id;
        private final int priority;

        AnimationLayer(ResourceLocation id, int priority) {
            this.id = id;
            this.priority = priority;
        }
    }


    public static void init() {
        if (initialized) return;
        initialized = true;

        for (AnimationLayer layer : AnimationLayer.values()) {
            if (AnimationLayer.ROTATION.equals(layer)) {
                continue;
            }
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(layer.id, layer.priority, p -> new ModifierLayer<>());
        }

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ROTATION_ANIMATION, 42,
                player -> new ModifierLayer<>(null, AdjustmentYRotModifier.getModifier(player)));

        MinecraftForge.EVENT_BUS.register(MeleeAnimationListener.class);
        MinecraftForge.EVENT_BUS.register(IdleAnimationHandler.class);
    }

    public static KeyframeAnimation getAnimation(ResourceLocation location, String name) {
        return IPAAssetManager.getAnimations(location, name).orElse(null);
    }

    public static void playIdleAnimation(AbstractClientPlayer player, ResourceLocation location, String name, AnimationLayer layer, int fadeInTicks) {
        KeyframeAnimation animation = getAnimation(location, name);
        if (animation == null) return;

        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            IAnimation current = modifierLayer.getAnimation();
            if (current instanceof KeyframeAnimationPlayer kap && kap.isActive()) {
                Object extraData = kap.getData().extraData.get("name");
                if (extraData instanceof String currentName && name.equals(currentName)) {
                    return;
                }
            }
            KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(animation, 0);
            animPlayer.getData().extraData.put("name", name);
            animPlayer.setFirstPersonMode(FirstPersonMode.DISABLED);

            // 抽象的一，但是总之it works
            // Create a frozen snapshot of the current animation state
            IAnimation frozenCurrent = current instanceof KeyframeAnimationPlayer kap ?
                new FrozenAnimationSnapshot(kap) : current;

            // Use custom fade modifier that always reads from beginAnimation, even if inactive
            var fadeModifier = new ForcedFadeModifier(fadeInTicks, Ease.INOUTSINE);
            fadeModifier.setBeginAnimation(frozenCurrent);
            modifierLayer.addModifierLast(fadeModifier);
            modifierLayer.setAnimation(animPlayer);
        }
    }

    /**
     * Frozen snapshot of an animation at a specific tick.
     * This prevents the animation from advancing during fade transitions.
     */
    private static class FrozenAnimationSnapshot implements IAnimation {
        private final KeyframeAnimationPlayer source;

        public FrozenAnimationSnapshot(KeyframeAnimationPlayer source) {
            this.source = source;
        }

        @Override
        public void tick() {
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
            return source.get3DTransform(modelName, type, 0, value0);
        }

        @Override
        public void setupAnim(float tickDelta) {
            source.setupAnim(0);
        }
    }

    private static class ForcedFadeModifier extends AbstractFadeModifier {
        private final Ease ease;
        
        protected ForcedFadeModifier(int length, Ease ease) {
            super(length);
            this.ease = ease;
        }
        
        @Override
        protected float getAlpha(String modelName, TransformType type, float progress) {
            return ease.invoke(progress);
        }
        
        @Override
        public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
            if (calculateProgress(tickDelta) > 1) {
                return super.get3DTransform(modelName, type, tickDelta, value0);
            }
            
            Vec3f animatedVec = super.get3DTransform(modelName, type, tickDelta, value0);
            float a = getAlpha(modelName, type, calculateProgress(tickDelta));

            Vec3f source = beginAnimation != null ? 
                beginAnimation.get3DTransform(modelName, type, tickDelta, value0) : value0;
            
            return animatedVec.scale(a).add(source.scale(1 - a));
        }
    }

    public static void playAttackAnimation(AbstractClientPlayer player, ResourceLocation location, String name, AnimationLayer layer, int fadeInTicks) {
        KeyframeAnimation animation = getAnimation(location, name);
        if (animation == null) return;

        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(animation, 0);
            animPlayer.getData().extraData.put("lr_attack_action", true);
            animPlayer.setFirstPersonMode(FirstPersonMode.DISABLED);
            modifierLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeInTicks, Ease.INOUTSINE), animPlayer);
        }
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> getLayer(AbstractClientPlayer player, AnimationLayer layer) {
        return (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(layer.id);
    }

    public static boolean isAttackAnimationActive(AbstractClientPlayer player, AnimationLayer layer) {
        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            IAnimation current = modifierLayer.getAnimation();
            if (current instanceof KeyframeAnimationPlayer kap) {
                Object extraData = kap.getData().extraData.get("lr_attack_action");
                if (extraData instanceof Boolean isAttack && isAttack) {
                    // Return false when animation reaches endTick, allowing idle animation to start
                    // This prevents the automatic fade-out to static pose
                    return kap.getCurrentTick() < kap.getData().endTick;
                }
            }
        }
        return false;
    }

    public static void stopAnimation(AbstractClientPlayer player, AnimationLayer layer, int fadeOutTicks) {
        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            modifierLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeOutTicks, Ease.INOUTSINE), null);
        }
    }

    @SuppressWarnings("unchecked")
    public static void enableRotationModifier(AbstractClientPlayer player, ResourceLocation location, int fadeInTicks) {
        KeyframeAnimation animation = getAnimation(location, "empty");
        if (animation == null) return;

        ModifierLayer<IAnimation> rotationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(ROTATION_ANIMATION);
        if (rotationLayer != null && rotationLayer.getAnimation() == null) {
            rotationLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeInTicks, Ease.INOUTSINE), new KeyframeAnimationPlayer(animation));
        }
    }

}
