package me.xjqsh.lrtactical.item.throwable;

import com.google.gson.annotations.SerializedName;

// 投掷物实体基础属性
public class EntityData {
    @SerializedName("life_time")
    private int lifeTime = 100;

    @SerializedName("gravity")
    private float gravity = 0.07f;

    @SerializedName("should_bounce")
    private boolean shouldBounce = true;

    @SerializedName("bounce_factor")
    private double bounceFactor = 0.75;

    @SerializedName("hit_damage")
    private float hitDamage = 1.0f;

    public int getLifeTime() {
        return lifeTime;
    }

    public float getGravity() {
        return gravity;
    }

    public boolean isShouldBounce() {
        return shouldBounce;
    }

    public double getBounceFactor() {
        return bounceFactor;
    }

    public float getHitDamage() {
        return hitDamage;
    }
}
