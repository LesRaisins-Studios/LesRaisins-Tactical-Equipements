package me.xjqsh.lrtactical.item.consumable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;

public class ConsumableData {
    @SerializedName("use_duration")
    private int useDuration = 32;

    @SerializedName("cooldown")
    private int cooldown = 0;

    @SerializedName("cooldown_category")
    private ResourceLocation cooldownCategory = null;

    @SerializedName("stack_size")
    private int stackSize = 1;

    @SerializedName("draw_time")
    private int drawTime = 0;

    @SerializedName("put_away_time")
    private int putAwayTime = 0;

    @SerializedName("heal")
    private float heal = 0f;

    @SerializedName("food")
    private int food = 0;

    @SerializedName("saturation")
    private float saturation = 0f;

    @SerializedName("effects")
    private List<EffectData> effects = Collections.emptyList();

    @SerializedName("remove_effects")
    private List<ResourceLocation> removeEffects = Collections.emptyList();

    public int getUseDuration() {
        return useDuration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public ResourceLocation getCooldownCategory() {
        return cooldownCategory;
    }

    public int getStackSize() {
        return stackSize;
    }

    public int getDrawTime() {
        return drawTime;
    }

    public int getPutAwayTime() {
        return putAwayTime;
    }

    public float getHeal() {
        return heal;
    }

    public int getFood() {
        return food;
    }

    public float getSaturation() {
        return saturation;
    }

    public List<EffectData> getEffects() {
        return effects;
    }

    public List<ResourceLocation> getRemoveEffects() {
        return removeEffects;
    }

    public static class EffectData {
        @SerializedName("id")
        private ResourceLocation id;

        @SerializedName("duration")
        private int duration = 0;

        @SerializedName("amplifier")
        private int amplifier = 0;

        @SerializedName("chance")
        private float chance = 1f;

        @SerializedName("ambient")
        private boolean ambient = false;

        @SerializedName("visible")
        private boolean visible = true;

        @SerializedName("show_icon")
        private boolean showIcon = true;

        public ResourceLocation getId() {
            return id;
        }

        public float getChance() {
            return chance;
        }

        public MobEffectInstance createInstance() {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
            if (effect == null) {
                return null;
            }
            return new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon);
        }
    }
}
