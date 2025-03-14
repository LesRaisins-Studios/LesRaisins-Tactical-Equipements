package me.xjqsh.lrtactical.item.throwable.flash;

import com.google.gson.annotations.SerializedName;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import org.jetbrains.annotations.NotNull;

// 闪光弹类投掷物属性配置
public class StunThrowableData extends ThrowableData {
    @SerializedName("stun")
    private StunData stunData = new StunData();

    @NotNull
    public StunThrowableData.StunData getStunData() {
        return stunData;
    }

    public static class StunData {
        // 半径内的实体会受到影响
        @SerializedName("radius")
        private float radius = 32f;

        @SerializedName("blind")
        private BlindData blind = new BlindData();

        @SerializedName("deafened")
        private DeafenedData deafened = new DeafenedData();

        public float getRadius() {
            return radius;
        }

        public BlindData getBlind() {
            return blind;
        }

        public DeafenedData getDeafened() {
            return deafened;
        }

        public int calcBlindDuration(double distance, double angle) {
            int mx = blind.getMaxDuration();
            int mn = blind.getMinDuration();
            int durationBlinded = (int) Math.round(mx - (mx - mn) * (distance / radius));
            double maxAngle = blind.getMaxAngle();
            double factor = blind.getViewAngleFactor();
            return (int) (durationBlinded * (1.0 - angle * (1.0 - factor) / maxAngle));
        }

        public int calcDeafenedDuration(double distance) {
            int mx = deafened.getMaxDuration();
            int mn = deafened.getMinDuration();
            return (int) Math.round(mx - (mx - mn) * (distance / radius));
        }
    }

    public static class DeafenedData {
        // 失聪的最大/最小持续时间，依照目标距离/最大距离在这两个值之间插值: Math.round(mx - (mx - mn) * (distance / radius))
        @SerializedName("max_duration")
        private int maxDuration = 200;

        @SerializedName("min_duration")
        private int minDuration = 10;

        public int getMaxDuration() {
            return maxDuration;
        }

        public int getMinDuration() {
            return minDuration;
        }
    }

    public static class BlindData {
        // 致盲的最大/最小持续时间，依照目标距离/最大距离在这两个值之间插值: Math.round(mx - (mx - mn) * (distance / radius))
        @SerializedName("max_duration")
        private int maxDuration = 200;

        @SerializedName("min_duration")
        private int minDuration = 10;

        // 目标实体视线与爆炸位置的最大夹角，超过这个角度则不会致盲
        @SerializedName("max_angle")
        private double maxAngle = 85;

        // 致盲时长在计算完距离后再进行依据视角衰减计算
        // 这个值代表在角度为maxAngle时，时长的百分比
        // finalDuration = durationBlinded * (1.0 - angle * (1.0 - viewAngleFactor) / maxAngle);
        @SerializedName("view_angle_factor")
        private double viewAngleFactor = 0.5;

        public int getMaxDuration() {
            return maxDuration;
        }

        public int getMinDuration() {
            return minDuration;
        }

        public double getMaxAngle() {
            return maxAngle;
        }

        public double getViewAngleFactor() {
            return viewAngleFactor;
        }
    }
}
