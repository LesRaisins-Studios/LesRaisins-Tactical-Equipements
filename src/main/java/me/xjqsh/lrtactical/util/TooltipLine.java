package me.xjqsh.lrtactical.util;

import net.minecraft.network.chat.Component;

/**
 * 一条 tooltip 数值行
 *
 * @param text        行文本
 * @param collapsible 是否为可折叠的效果行：默认最多显示前 3 条，按住 Shift 显示全部
 */
public record TooltipLine(Component text, boolean collapsible) {
    public static TooltipLine normal(Component text) {
        return new TooltipLine(text, false);
    }

    public static TooltipLine collapsible(Component text) {
        return new TooltipLine(text, true);
    }
}
