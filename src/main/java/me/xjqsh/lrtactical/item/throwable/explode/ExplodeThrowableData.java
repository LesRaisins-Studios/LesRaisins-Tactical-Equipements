package me.xjqsh.lrtactical.item.throwable.explode;

import com.google.gson.annotations.SerializedName;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import org.jetbrains.annotations.NotNull;

// 爆炸类投掷物属性配置
public class ExplodeThrowableData extends ThrowableData {
    @SerializedName("explode")
    private ExplodeData explode = new ExplodeData();

    @NotNull
    public ExplodeData getExplode() {
        return explode;
    }

    public static class ExplodeData {
        @SerializedName("radius")
        private float radius = 5.5f;

        @SerializedName("damage")
        private double damage = 22.0;

        public float getRadius() {
            return radius;
        }

        public double getDamage() {
            return damage;
        }
    }
}
