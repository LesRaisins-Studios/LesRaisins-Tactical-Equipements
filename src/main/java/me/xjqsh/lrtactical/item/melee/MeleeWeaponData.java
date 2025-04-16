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
}
