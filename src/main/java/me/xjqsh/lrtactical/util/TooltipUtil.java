package me.xjqsh.lrtactical.util;

import java.util.Locale;

/**
 * Tooltip 文本生成的公共工具
 */
public final class TooltipUtil {
    private TooltipUtil() {
    }

    /**
     * 将数值格式化为最多一位小数的字符串
     */
    public static String format(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /**
     * 将伤害倍率格式化为 xN.NN 形式
     */
    public static String formatFactor(float factor) {
        return String.format(Locale.ROOT, "x%.2f", factor);
    }

    /**
     * 将 tick 时长转换为秒显示（1s = 20 tick），整数秒不带小数
     */
    public static String formatTicks(int ticks) {
        if (ticks % 20 == 0) {
            return ticks / 20 + "s";
        }
        return String.format(Locale.ROOT, "%.2fs", ticks / 20.0);
    }
}
