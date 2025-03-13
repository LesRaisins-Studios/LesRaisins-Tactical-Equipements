package me.xjqsh.lrtactical.client.overlay;

import me.xjqsh.lrtactical.api.item.ICustomItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class UsingProgressOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getUseItem();
        if (stack.getItem() instanceof ICustomItem item) {
            float maxTick = item.getMaxUsingTick(stack);
            int usingTick = player.getTicksUsingItem();
            float progress = Math.min(1f, usingTick / maxTick);
            int x = screenWidth / 2 - 16;
            int y = screenHeight / 2 + 16;
            int alpha = 0x80;
            if (progress == 1f) {
                alpha = (int) (80 + 80 * Math.sin(usingTick / 2f));
            }
            guiGraphics.fill(x, y, (int) (x + progress * 32), y + 4, 0xFFFFFF | (alpha << 24));
        }
    }
}
