package me.xjqsh.lrtactical.item.melee;

import com.google.gson.annotations.SerializedName;

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
}
