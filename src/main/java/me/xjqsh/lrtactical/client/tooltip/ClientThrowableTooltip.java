package me.xjqsh.lrtactical.client.tooltip;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.inventory.tooltip.ThrowableTooltip;

/**
 * 投掷物 tooltip 的客户端渲染组件
 * 数值行由 {@link me.xjqsh.lrtactical.item.throwable.ThrowableData#getTooltipLines()} 生成
 */
public class ClientThrowableTooltip extends AbstractClientItemTooltip {
    public ClientThrowableTooltip(ThrowableTooltip tooltip) {
        LrTacticalAPI.getThrowableIndex(tooltip.getStack()).ifPresent(index ->
                this.build(index.getTooltip(), index.getData().getTooltipLines()));
    }
}
