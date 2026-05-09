package me.xjqsh.lrtactical.client.input;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.CCancelToggleConsumableUse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = EquipmentMod.MOD_ID)
public class ConsumableInputHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.keyUse.matches(event.getKey(), event.getScanCode())) {
                tryCancelToggleUse(mc);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.keyUse.matchesMouse(event.getButton()) && tryCancelToggleUse(mc)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean tryCancelToggleUse(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || !player.isUsingItem()) {
            return false;
        }

        ItemStack useItem = player.getUseItem();
        if (!(useItem.getItem() instanceof IConsumable)) {
            return false;
        }

        ConsumableIndex index = LrTacticalAPI.getConsumableIndex(useItem).orElse(null);
        if (index == null || !index.getData().isToggleUse()) {
            return false;
        }

        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem != useItem) {
            return false;
        }

        player.stopUsingItem();
        NetworkHandler.sendToServer(new CCancelToggleConsumableUse());
        while (mc.options.keyUse.consumeClick()) {
        }
        return true;
    }
}
