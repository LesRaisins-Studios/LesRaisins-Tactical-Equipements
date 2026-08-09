package me.xjqsh.lrtactical.client.tooltip;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.inventory.tooltip.ConsumableTooltip;

/**
 * 消耗品 tooltip 的客户端渲染组件
 * 数值行由 {@link me.xjqsh.lrtactical.item.consumable.ConsumableData#getTooltipLines()} 生成
 */
public class ClientConsumableTooltip extends AbstractClientItemTooltip {
    public ClientConsumableTooltip(ConsumableTooltip tooltip) {
        LrTacticalAPI.getConsumableIndex(tooltip.getStack()).ifPresent(index ->
                this.build(index.getTooltip(), index.getData().getTooltipLines()));
    }
}
