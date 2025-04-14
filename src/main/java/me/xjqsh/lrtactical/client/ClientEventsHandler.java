package me.xjqsh.lrtactical.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.input.InteractKey;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.config.ClientConfig;
import me.xjqsh.lrtactical.init.ModEffects;
import me.xjqsh.lrtactical.item.MeleeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = EquipmentMod.MOD_ID)
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
        int color = (ClientConfig.BLACK_FLASH.get() ? 0x000000 : 0xFFFFFF) + (alpha << 24);

        GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        graphics.fill(0, 0, width, height, color);

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // 只要主手有枪，那么禁止交互
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() instanceof ICustomItem customItem) {
            boolean flag = (event.isAttack() && customItem.shouldBlockAttack())
                    || (event.isUseItem() && customItem.shouldBlockUse())
                    || (event.isPickBlock() && customItem.shouldBlockUse());
            if (flag) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

}
