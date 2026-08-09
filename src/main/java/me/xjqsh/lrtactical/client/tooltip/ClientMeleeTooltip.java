package me.xjqsh.lrtactical.client.tooltip;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.inventory.tooltip.MeleeTooltip;

/**
 * 近战武器 tooltip 的客户端渲染组件
 * 数值行由 {@link me.xjqsh.lrtactical.item.melee.MeleeWeaponData#getTooltipLines()} 生成
 */
public class ClientMeleeTooltip extends AbstractClientItemTooltip {
    public ClientMeleeTooltip(MeleeTooltip tooltip) {
        LrTacticalAPI.getMeleeIndex(tooltip.getStack()).ifPresent(index ->
                this.build(index.getTooltip(), index.getData().getTooltipLines()));
    }
}
