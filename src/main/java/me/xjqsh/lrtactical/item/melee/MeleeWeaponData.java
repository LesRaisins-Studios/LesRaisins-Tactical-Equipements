package me.xjqsh.lrtactical.item.melee;

import com.google.gson.annotations.SerializedName;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.util.TooltipLine;
import me.xjqsh.lrtactical.util.TooltipUtil;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MeleeWeaponData {
    @SerializedName("draw_time")
    private int drawTime;

    @SerializedName("put_away_time")
    private int putAwayTime;

    @SerializedName("attack")
    private CombatData attackInfo = new CombatData();

    @SerializedName("attributes")
    private AttributeData attributes = new AttributeData();

    @SerializedName("max_durability")
    private int maxDurability = 0;

    @SerializedName("enchantment_value")
    private int enchantmentValue = 14;

    public int getPutAwayTime() {
        return putAwayTime;
    }

    public int getDrawTime() {
        return drawTime;
    }

    public CombatData getAttackInfo() {
        return attackInfo;
    }

    public AttributeData getRawAttributes() {
        return attributes;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    /**
     * 生成近战武器的 tooltip 信息行
     */
    public List<TooltipLine> getTooltipLines() {
        List<TooltipLine> lines = new ArrayList<>();
        for (MeleeAction action : MeleeAction.values()) {
            CombatData.MeleeAttackInfo attackInfo = attackInfoOf(action);
            if (attackInfo == null) {
                continue;
            }
            String actionKey = action == MeleeAction.LEFT
                    ? "tooltip.lrtactical.melee.attack_left"
                    : "tooltip.lrtactical.melee.attack_right";
            lines.add(TooltipLine.normal(Component.translatable(
                    "tooltip.lrtactical.melee.action_line",
                    Component.translatable(actionKey),
                    TooltipUtil.formatFactor(attackInfo.getFactor()),
                    TooltipUtil.formatTicks(attackInfo.getCooldown())
            )));
        }
        return lines;
    }

    private CombatData.MeleeAttackInfo attackInfoOf(MeleeAction action) {
        if (attackInfo == null) {
            return null;
        }
        return attackInfo.getAttackInfo(action);
    }
}
