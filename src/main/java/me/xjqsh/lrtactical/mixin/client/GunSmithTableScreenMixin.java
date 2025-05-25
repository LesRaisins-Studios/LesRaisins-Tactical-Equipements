package me.xjqsh.lrtactical.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.inventory.GunSmithTableMenu;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GunSmithTableScreen.class)
public abstract class GunSmithTableScreenMixin extends AbstractContainerScreen<GunSmithTableMenu> {


    public GunSmithTableScreenMixin(GunSmithTableMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Inject(method = "renderPackInfo", at = @At("HEAD"), remap = false, cancellable = true)
    private void renderPackInfo(GuiGraphics gui, GunSmithTableRecipe recipe, CallbackInfo ci) {
        ItemStack output = recipe.getOutput();
        Item item = output.getItem();
        ResourceLocation id;
        if (item instanceof ICustomItem customItem) {
            id = customItem.getId(output);
        } else {
            return;
        }

        PackInfo packInfo = ClientAssetsManager.INSTANCE.getPackInfo(id);
        PoseStack poseStack = gui.pose();
        if (packInfo != null) {
            poseStack.pushPose();
            poseStack.scale(0.75f, 0.75f, 1);
            Component nameText = Component.translatable(packInfo.getName());
            gui.drawString(font, nameText, (int) ((leftPos + 6) / 0.75f), (int) ((topPos + 122) / 0.75f), ChatFormatting.DARK_GRAY.getColor(), false);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 1);

            int offsetX = (leftPos + 6) * 2;
            int offsetY = (topPos + 123) * 2;
            int nameWidth = font.width(nameText);
            Component ver = Component.literal("v" + packInfo.getVersion()).withStyle(ChatFormatting.UNDERLINE);
            gui.drawString(font, ver, (int) (offsetX + nameWidth * 0.75f / 0.5f + 5), offsetY, ChatFormatting.DARK_GRAY.getColor(), false);
            offsetY += 14;

            String descKey = packInfo.getDescription();
            if (StringUtils.isNoneBlank(descKey)) {
                Component desc = Component.translatable(descKey);
                List<FormattedCharSequence> split = font.split(desc, 245);
                for (FormattedCharSequence charSequence : split) {
                    gui.drawString(font, charSequence, offsetX, offsetY, ChatFormatting.DARK_GRAY.getColor(), false);
                    offsetY += font.lineHeight;
                }
                offsetY += 3;
            }

            gui.drawString(font, Component.translatable("gui.tacz.gun_smith_table.license")
                            .append(Component.literal(packInfo.getLicense()).withStyle(ChatFormatting.DARK_GRAY)),
                    offsetX, offsetY, ChatFormatting.DARK_GRAY.getColor(), false);
            offsetY += 12;

            List<String> authors = packInfo.getAuthors();
            if (!authors.isEmpty()) {
                gui.drawString(font, Component.translatable("gui.tacz.gun_smith_table.authors")
                                .append(Component.literal(StringUtils.join(authors, ", ")).withStyle(ChatFormatting.DARK_GRAY)),
                        offsetX, offsetY, ChatFormatting.DARK_GRAY.getColor(), false);
                offsetY += 12;
            }

            gui.drawString(font, Component.translatable("gui.tacz.gun_smith_table.date")
                            .append(Component.literal(packInfo.getDate()).withStyle(ChatFormatting.DARK_GRAY)),
                    offsetX, offsetY, ChatFormatting.DARK_GRAY.getColor(), false);

            poseStack.popPose();
        } else {
            ResourceLocation recipeId = recipe.getId();
            gui.drawString(font, Component.translatable("gui.tacz.gun_smith_table.error").withStyle(ChatFormatting.DARK_RED), leftPos + 6, topPos + 122, 0xAF0000, false);
            gui.drawString(font, Component.translatable("gui.tacz.gun_smith_table.error.id", recipeId.toString()).withStyle(ChatFormatting.DARK_RED), leftPos + 6, topPos + 134, 0xFFFFFF, false);
            PackInfo errorPackInfo = ClientAssetsManager.INSTANCE.getPackInfo(id);
            if (errorPackInfo != null) {
                gui.drawString(font, Component.translatable(errorPackInfo.getName()).withStyle(ChatFormatting.DARK_RED), leftPos + 6, topPos + 146, 0xAF0000, false);
            }
        }
        ci.cancel();
    }
}
