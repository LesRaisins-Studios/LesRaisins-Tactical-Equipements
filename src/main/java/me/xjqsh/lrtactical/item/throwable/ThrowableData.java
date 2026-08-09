package me.xjqsh.lrtactical.item.throwable;

import com.google.gson.annotations.SerializedName;
import me.xjqsh.lrtactical.util.TooltipLine;
import me.xjqsh.lrtactical.util.TooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// 投掷物属性配置
public class ThrowableData {
    @SerializedName("prepare_time")
    private int prepareTime = 10;
    
    @SerializedName("cookable")
    private boolean cookable = false;

    @SerializedName("initial_speed")
    private double initialSpeed = 1.5;

    @SerializedName("cooldown")
    private int cooldown = 40;

    @SerializedName("cooldown_category")
    private ResourceLocation cooldownCategory = null;

    @SerializedName("stack_size")
    private int stackSize = 1;

    @SerializedName("entity")
    private EntityData entityData = new EntityData();

    @SerializedName("put_away_time")
    private long putAwayTime = 0;

    public int getPrepareTime() {
        return prepareTime;
    }

    public double getInitialSpeed() {
        return initialSpeed;
    }

    public int getCooldown() {
        return cooldown;
    }

    @Nullable
    public ResourceLocation getCooldownCategory() {
        return cooldownCategory;
    }

    public int getStackSize() {
        return stackSize;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    public long getPutAwayTime() {
        return putAwayTime;
    }

    public boolean isCookable() {
        return cookable;
    }

    /**
     * 生成投掷物的通用 tooltip 信息行，子类可覆写并追加类型专属信息
     */
    public List<TooltipLine> getTooltipLines() {
        List<TooltipLine> lines = new ArrayList<>();
        if (getCooldown() > 0) {
            lines.add(TooltipLine.normal(Component.translatable("tooltip.lrtactical.throwable.cooldown",
                    TooltipUtil.formatTicks(getCooldown()))));
        }
        if (isCookable()) {
            lines.add(TooltipLine.normal(Component.translatable("tooltip.lrtactical.throwable.cookable")));
        }
        // 基础类型没有更具体的子类信息（如烟雾弹），此时生命时长即持续时间
        if (getClass() == ThrowableData.class && getEntityData().getLifeTime() > 0) {
            lines.add(TooltipLine.normal(Component.translatable("tooltip.lrtactical.throwable.life_time",
                    TooltipUtil.formatTicks(getEntityData().getLifeTime()))));
        }
        return lines;
    }
}
