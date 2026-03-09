package me.xjqsh.lrtactical.compat.player_animator;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.event.MeleePreAttackEvent;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class MeleeAnimationListener {

    @SubscribeEvent
    public static void onMeleeAnimation(MeleePreAttackEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (mc.level.getEntity(event.getPlayerId()) instanceof AbstractClientPlayer player) {
            playStateAnimation(player, event.getState(), event.getActionCount(), event.getAnimationId());
        }
    }

    private static void playStateAnimation(AbstractClientPlayer player, MeleeAction state, int actionCount,
                                           ResourceLocation itemId) {
        LrTacticalAPI.getMeleeDisplay(itemId).ifPresent(display -> {
            ThirdPersonAnimationConfig config = display.getThirdPersonAnimation();
            if (config == null) return;

            ResourceLocation path = config.getAnimationPath();
            if (path == null) return;

            String action = state.getId();
            var layer = PlayerAnimatorIntegration.AnimationLayer.UPPER;
            int fade = 4;

            String anim = config.getAnimation(layer, action, actionCount);
            if (anim != null) {
                PlayerAnimatorIntegration.playAttackAnimation(player, path, anim, layer, fade);
            }
        });
    }

}
