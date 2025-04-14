package me.xjqsh.lrtactical.item.melee;

import com.google.gson.annotations.SerializedName;
import me.xjqsh.lrtactical.api.collision.ITargetFilter;

public record MeleeAttackInfo(
    @SerializedName("factor")
    double factor,

    @SerializedName("knockback")
    double knockback,

    @SerializedName("cooldown")
    int cooldown,

    @SerializedName("delay")
    int delay,

    @SerializedName("hitbox")
    ITargetFilter hitbox
) {
}
