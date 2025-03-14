package me.xjqsh.lrtactical.mixin.client;

import me.xjqsh.lrtactical.client.ClientEventsHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
            )
    )
    public void renderFlash(float pPartialTicks, long pNanoTime, boolean pRenderLevel, CallbackInfo ci) {
        ClientEventsHandler.afterLevel(pPartialTicks, pNanoTime, pRenderLevel);
    }
}
