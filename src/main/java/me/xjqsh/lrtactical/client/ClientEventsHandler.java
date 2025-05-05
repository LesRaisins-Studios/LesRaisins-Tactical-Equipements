package me.xjqsh.lrtactical.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.input.InteractKey;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.config.ClientConfig;
import me.xjqsh.lrtactical.init.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = EquipmentMod.MOD_ID)
public class ClientEventsHandler {
    @SubscribeEvent
    public static void tickAnimation(TickEvent.ClientTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        LrTacticalAPI.getMeleeDisplay(mainHandItem).ifPresent(index -> {
            var animationStateMachine = index.getStateMachine();
            // 群组服切世界导致的特殊 BUG 处理，正常情况不会遇到此问题
            if (player.input == null) {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_IDLE);
                return;
            }
            if (!player.isMovingSlowly() && player.isSprinting()) {
                // 如果玩家正在移动，播放移动动画，否则播放 idle 动画
                animationStateMachine.trigger(GunAnimationConstant.INPUT_RUN);
            } else if (!player.isMovingSlowly() && player.input.getMoveVector().length() > 0.01) {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_WALK);
            } else {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_IDLE);
            }
        });
    }


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

        // 当交互键按下时，允许交互
        if (InteractKey.INTERACT_KEY.isDown()) {
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
