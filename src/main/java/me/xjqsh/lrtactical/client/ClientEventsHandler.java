package me.xjqsh.lrtactical.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xjqsh.lrtactical.init.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class ClientEventsHandler {
    public static void afterLevel(float pPartialTicks, long pNanoTime, boolean pRenderLevel) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (!pRenderLevel || mc.level == null || player == null) {
            return;
        }
        MobEffectInstance effect = player.getEffect(ModEffects.BLIND.get());
        if (effect == null) {
            return;
        }

        int tickRemain = effect.getDuration();

        int alpha = tickRemain > 100 ? 255 : (int) (tickRemain / 100f * 255f);
        int color = 0xFFFFFF + (alpha << 24);

        GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        int width = mc.getWindow().getScreenWidth();
        int height = mc.getWindow().getScreenHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        graphics.fill(0, 0, width, height, color);

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

}
