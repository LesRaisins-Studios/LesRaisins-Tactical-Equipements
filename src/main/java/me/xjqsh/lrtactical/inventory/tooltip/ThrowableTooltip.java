package me.xjqsh.lrtactical.inventory.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * 投掷物自定义 tooltip 的数据载体
 * 由 {@link me.xjqsh.lrtactical.item.ThrowableItem#getTooltipImage} 返回，
 * 在客户端由 ClientTooltipComponent 渲染（参见 me.xjqsh.lrtactical.client.tooltip.ClientThrowableTooltip）
 */
public class ThrowableTooltip implements TooltipComponent {
    private final ItemStack stack;

    public ThrowableTooltip(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}
