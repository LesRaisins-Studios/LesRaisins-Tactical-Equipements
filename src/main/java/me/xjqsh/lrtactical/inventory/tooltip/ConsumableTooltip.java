package me.xjqsh.lrtactical.inventory.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * 消耗品自定义 tooltip 的数据载体
 * 由 {@link me.xjqsh.lrtactical.item.ConsumableItem#getTooltipImage} 返回，
 * 在客户端由 ClientTooltipComponent 渲染（参见 me.xjqsh.lrtactical.client.tooltip.ClientConsumableTooltip）
 */
public class ConsumableTooltip implements TooltipComponent {
    private final ItemStack stack;

    public ConsumableTooltip(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}
