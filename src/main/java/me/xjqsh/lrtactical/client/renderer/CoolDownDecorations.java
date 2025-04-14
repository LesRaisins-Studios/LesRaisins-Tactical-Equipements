package me.xjqsh.lrtactical.client.renderer;

import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.capability.CustomItemCoolDownsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class CoolDownDecorations implements IItemDecorator {
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int pX, int pY) {
        Player player = Minecraft.getInstance().player;
        float partialTicks = Minecraft.getInstance().getFrameTime();
        if (player == null) {
            return false;
        }
        if (stack.getItem() instanceof ICustomItem item) {
            float f = item.getCoolDownId(stack)
                    .map(id -> player.getCapability(CustomItemCoolDownsProvider.CAPABILITY)
                            .map(cap -> cap.getCooldownPercent(id, partialTicks))
                            .orElse(0f)
                    ).orElse(0f);
            if (f > 0.0F) {
                int i1 = pY + Mth.floor(16.0F * (1.0F - f));
                int j1 = i1 + Mth.ceil(16.0F * f);
                guiGraphics.fill(RenderType.guiOverlay(), pX, i1, pX + 16, j1, Integer.MAX_VALUE);
            }
            return true;
        }
        return false;
    }
}
