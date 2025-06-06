package me.xjqsh.lrtactical.client.overlay;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class UsingProgressOverlay implements IGuiOverlay {
    public static final ResourceLocation ARROW_TEXTURE = new ResourceLocation(EquipmentMod.MOD_ID, "textures/gui/arrow.png");
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
            int alpha;
            if (progress == 1f) {
                alpha = (int) (80 + 80 * Math.sin(usingTick / 2f));
            } else {
                alpha = 0x80;
            }
            guiGraphics.fill(x, y, (int) (x + progress * 32), y + 4, 0xFFFFFF | (alpha << 24));
            if (stack.getItem() instanceof IThrowable throwable) {
                throwable.getThrowableIndex(stack).ifPresent(index -> {
                    ThrowableData data = index.getData();
                    if (data.isCookable() && player.getTicksUsingItem() >= data.getPrepareTime()) {
                        int cookTime = player.getTicksUsingItem() - data.getPrepareTime();
                        float cookProgress = Math.min(1f, cookTime / (float) data.getEntityData().getLifeTime());
                        guiGraphics.fill(x, y, (int) (x + cookProgress * 32), y + 4, 0xFF0000 | (alpha << 24));
                    }
                });
                if (player.isCrouching()) {
                    guiGraphics.blit(ARROW_TEXTURE, x + 12, y + 6, 0, 0, 8, 4, 8, 4);
                }
            }
        }

        player.getCapability(CombatPropertiesProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getCoolDownTick() > 0) {
                float maxTick = cap.getLastMaxTick();
                float progress = 1 - Math.min(1f, cap.getCoolDownTick() / maxTick);
                int x = screenWidth / 2 - 16;
                int y = screenHeight / 2 + 16;
                int alpha = 0x80;
                if (progress == 1f) {
                    alpha = (int) (80 + 80 * Math.sin(cap.getCoolDownTick() / 2f));
                }
                guiGraphics.fill(x, y, (int) (x + progress * 32), y + 4, 0xFFFFFF | (alpha << 24));
            }
        });
    }
}
