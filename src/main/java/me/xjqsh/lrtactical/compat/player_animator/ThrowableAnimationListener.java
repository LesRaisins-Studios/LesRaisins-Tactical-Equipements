package me.xjqsh.lrtactical.compat.player_animator;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.network.message.SThrowableAnimationSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ThrowableAnimationListener {
    private static final Map<String, Integer> indexMap = new HashMap<>();

    @SubscribeEvent
    public static void onThrowableAnimation(SThrowableAnimationSync.ClientEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        AbstractClientPlayer player = (AbstractClientPlayer) mc.level.getEntity(event.getPlayerId());
        if (player == null || player == mc.player) return;

        playStateAnimation(player, event.getPlayerId(), event.getState(), event.getAnimationId());
    }

    private static void playStateAnimation(AbstractClientPlayer player, int playerId, SThrowableAnimationSync.ThrowableState state, ResourceLocation itemId) {
        LrTacticalAPI.getThrowableDisplay(itemId).ifPresent(display -> {
            ThirdPersonAnimationConfig config = display.getThirdPersonAnimation();
            if (config == null) return;

            ResourceLocation path = config.getAnimationPath();
            if (path == null) return;

            String action = getAction(state);
            PlayerAnimatorIntegration.AnimationLayer layer = PlayerAnimatorIntegration.AnimationLayer.UPPER;
            boolean loop = state == SThrowableAnimationSync.ThrowableState.IDLE;
            int fade = loop ? 8 : 2;

            String key = playerId + ":" + itemId + ":" + action;
            int index = indexMap.getOrDefault(key, 0);

            String anim = config.getAnimation(layer, action, index);
            if (anim != null) {
                PlayerAnimatorIntegration.playAnimation(player, path, anim, layer, fade);
                if (config.hasMultiple(layer, action)) {
                    indexMap.put(key, (index + 1) % config.getCount(layer, action));
                }
            }
        });
    }

    private static String getAction(SThrowableAnimationSync.ThrowableState state) {
        return switch (state) {
            case IDLE -> "idle";
            case PREPARE -> "prepare";
            case THROW -> "throw";
        };
    }
}
