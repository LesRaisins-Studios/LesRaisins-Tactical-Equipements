package me.xjqsh.lrtactical.compat.player_animator;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.client.resource.display.MeleeDisplayInstance;
import me.xjqsh.lrtactical.compat.player_animator.PlayerAnimatorIntegration.AnimationLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class IdleAnimationHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (var clientPlayer : mc.level.players()) {
            // 睡觉、爬梯、游泳、鞘翅飞行不播放第三人称动画
            if (clientPlayer.getPose() == Pose.SLEEPING || clientPlayer.onClimbable() || clientPlayer.isSwimming() || clientPlayer.getPose() == Pose.FALL_FLYING) {
                stopAll(clientPlayer);
                continue;
            }

            ItemStack stack = clientPlayer.getMainHandItem();
            ThirdPersonAnimationConfig config = getConfig(stack);

            if (config == null) {
                stopAll(clientPlayer);
                continue;
            }

            if (!PlayerAnimatorIntegration.isAttackAnimationActive(clientPlayer, AnimationLayer.UPPER)) {
                applyIdleAnimationFor(clientPlayer, config);
            }
            applyLowerBodyAnimation(clientPlayer, config);
            enableRotationModifier(clientPlayer, config);
        }
    }

    public static void stopAll(AbstractClientPlayer clientPlayer) {
        PlayerAnimatorIntegration.stopAnimation(clientPlayer, AnimationLayer.UPPER, 8);
        PlayerAnimatorIntegration.stopAnimation(clientPlayer, AnimationLayer.LOWER, 8);
        PlayerAnimatorIntegration.stopAnimation(clientPlayer, AnimationLayer.ROTATION, 4);
    }

    public static void enableRotationModifier(AbstractClientPlayer player, @NotNull ThirdPersonAnimationConfig config) {
        ResourceLocation basePath = config.getAnimationPath();

        if (basePath != null) {
            PlayerAnimatorIntegration.enableRotationModifier(player, basePath, 4);
        }
    }

    public static void applyIdleAnimationFor(AbstractClientPlayer player, @NotNull ThirdPersonAnimationConfig config) {
        String state = getPlayerMovementState(player);
        ResourceLocation basePath = config.getAnimationPath();
        String animName = config.getAnimation(AnimationLayer.UPPER, state, 0);
        if (basePath != null && animName != null) {
            PlayerAnimatorIntegration.playIdleAnimation(player, basePath, animName, AnimationLayer.UPPER, 4);
        }
    }

    private static void applyLowerBodyAnimation(AbstractClientPlayer player, @NotNull ThirdPersonAnimationConfig config) {
        String state = getPlayerMovementState(player);
        ResourceLocation basePath = config.getAnimationPath();
        String animName = config.getAnimation(AnimationLayer.LOWER, state, 0);
        if (basePath != null && animName != null) {
            PlayerAnimatorIntegration.playIdleAnimation(player, basePath, animName, AnimationLayer.LOWER, 8);
        }
    }

    private static String getPlayerMovementState(AbstractClientPlayer player) {
        if (!player.onGround()) return player.getDeltaMovement().y > 0 ? "jump" : "fall";
        double speed = player.getDeltaMovement().horizontalDistanceSqr();
        if (player.isCrouching()) return speed > 0.001 ? "sneak_walk" : "sneak";
        if (player.isSprinting()) return "sprint";
        if (speed > 0.001) return speed > 0.05 ? "run" : "walk";
        return "idle";
    }

    private static ThirdPersonAnimationConfig getConfig(ItemStack stack) {
        if (stack.getItem() instanceof IMeleeWeapon) {
            return LrTacticalAPI.getMeleeDisplay(stack).map(MeleeDisplayInstance::getThirdPersonAnimation).orElse(null);
        }
        return null;
    }
}
