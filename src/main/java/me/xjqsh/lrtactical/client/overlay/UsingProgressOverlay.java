package me.xjqsh.lrtactical.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
            
            // 检查是否是切换模式的消耗品
            boolean isToggleMode = false;
            if (stack.getItem() instanceof IConsumable) {
                ConsumableIndex index = LrTacticalAPI.getConsumableIndex(stack).orElse(null);
                if (index != null && index.getData().isToggleUse()) {
                    isToggleMode = true;
                }
            }
            
            // 切换模式使用不同颜色的进度条
            int barColor = isToggleMode ? 0x00FF00 : 0xFFFFFF;
            guiGraphics.fill(x, y, (int) (x + progress * 32), y + 4, barColor | (alpha << 24));
            
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
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1f, 1f, 1f, 0.7f);
                    guiGraphics.blit(ARROW_TEXTURE, x + 12, y + 6, 0, 0, 8, 4, 8, 4);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    RenderSystem.disableBlend();
                }
            }
            
            // 为切换模式显示提示文本
            if (isToggleMode) {
                Component hint = Component.translatable("overlay.lrtactical.consumable.toggle_hint");
                int textWidth = mc.font.width(hint);
                guiGraphics.drawString(mc.font, hint, screenWidth / 2 - textWidth / 2, y + 8, 0xFFFFFF, true);
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
