package me.xjqsh.lrtactical.client.tooltip;

import me.xjqsh.lrtactical.util.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义物品 tooltip 渲染的公共实现：
 * 描述文本（灰色）+ 数值信息行（金色）
 * 折叠规则（按住 Shift 展开全部）：
 * - 描述文本默认最多显示 3 行，不硬截断，超出部分折叠
 * - 可折叠的效果行默认最多显示前 3 条，多余显示 "…还有 X 项"
 * 数值行由数据类 {@link me.xjqsh.lrtactical.item.consumable.ConsumableData#getTooltipLines()} 等生成
 */
public abstract class AbstractClientItemTooltip implements ClientTooltipComponent {
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_DESC_WIDTH = 300;
    private static final int MAX_VISIBLE_DESC_LINES = 3;
    private static final int MAX_VISIBLE_EFFECT_LINES = 3;

    /** 完整描述文本（未截断，按宽度换行） */
    private @Nullable List<FormattedCharSequence> desc;
    /** 核心数值行，始终显示 */
    private final List<Component> normalLines = new ArrayList<>();
    /** 可折叠的效果行 */
    private final List<Component> effectLines = new ArrayList<>();
    /** 效果行折叠时的省略提示行 */
    private @Nullable Component moreHint;
    /** 存在折叠内容时，最后统一显示的 Shift 提示行 */
    private @Nullable Component shiftHint;

    private int maxWidth;

    /**
     * @param tooltipKey 描述翻译键，可为 null
     * @param lines      数值信息行
     */
    protected void build(@Nullable String tooltipKey, List<TooltipLine> lines) {
        Font font = Minecraft.getInstance().font;
        if (tooltipKey != null && !tooltipKey.isEmpty()) {
            this.desc = font.split(Component.translatable(tooltipKey), MAX_DESC_WIDTH);
            for (FormattedCharSequence sequence : this.desc) {
                this.maxWidth = Math.max(font.width(sequence), this.maxWidth);
            }
        }
        for (TooltipLine line : lines) {
            this.maxWidth = Math.max(font.width(line.text()), this.maxWidth);
            if (line.collapsible()) {
                this.effectLines.add(line.text());
            } else {
                this.normalLines.add(line.text());
            }
        }
        boolean descCollapsed = this.desc != null && this.desc.size() > MAX_VISIBLE_DESC_LINES;
        boolean effectsCollapsed = this.effectLines.size() > MAX_VISIBLE_EFFECT_LINES;
        if (effectsCollapsed) {
            this.moreHint = Component.translatable("tooltip.lrtactical.more_lines",
                    this.effectLines.size() - MAX_VISIBLE_EFFECT_LINES);
            this.maxWidth = Math.max(font.width(this.moreHint), this.maxWidth);
        }
        if (descCollapsed || effectsCollapsed) {
            this.shiftHint = Component.translatable("tooltip.lrtactical.shift_hint")
                    .withStyle(ChatFormatting.ITALIC);
            this.maxWidth = Math.max(font.width(this.shiftHint), this.maxWidth);
        }
    }

    private boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    private List<FormattedCharSequence> getVisibleDesc() {
        if (this.desc == null) {
            return List.of();
        }
        if (isShiftDown() || this.desc.size() <= MAX_VISIBLE_DESC_LINES) {
            return this.desc;
        }
        return this.desc.subList(0, MAX_VISIBLE_DESC_LINES);
    }

    private List<Component> getVisibleLines() {
        boolean shift = isShiftDown();
        List<Component> visible = new ArrayList<>(this.normalLines.size() + this.effectLines.size() + 2);
        visible.addAll(this.normalLines);
        boolean collapsed = !shift && this.effectLines.size() > MAX_VISIBLE_EFFECT_LINES;
        if (collapsed) {
            visible.addAll(this.effectLines.subList(0, MAX_VISIBLE_EFFECT_LINES));
            if (this.moreHint != null) {
                visible.add(this.moreHint);
            }
        } else {
            visible.addAll(this.effectLines);
        }
        // 描述被折叠时同样显示 Shift 提示
        if (!shift && this.desc != null && this.desc.size() > MAX_VISIBLE_DESC_LINES) {
            collapsed = true;
        }
        if (collapsed && this.shiftHint != null) {
            visible.add(this.shiftHint);
        }
        return visible;
    }

    @Override
    public int getHeight() {
        int height = 0;
        if (!getVisibleDesc().isEmpty()) {
            height += getVisibleDesc().size() * LINE_HEIGHT + 2;
        }
        height += getVisibleLines().size() * LINE_HEIGHT;
        return height;
    }

    @Override
    public int getWidth(Font font) {
        return this.maxWidth;
    }

    @Override
    public void renderText(Font font, int pX, int pY, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        int yOffset = pY;
        List<FormattedCharSequence> visibleDesc = getVisibleDesc();
        if (!visibleDesc.isEmpty()) {
            yOffset += 2;
            for (FormattedCharSequence sequence : visibleDesc) {
                font.drawInBatch(sequence, pX, yOffset, 0xaaaaaa, false, matrix4f, bufferSource,
                        Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += LINE_HEIGHT;
            }
        }
        for (Component component : getVisibleLines()) {
            font.drawInBatch(component, pX, yOffset, 0xffaa00, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += LINE_HEIGHT;
        }
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
        // 无需渲染额外图片
    }
}
