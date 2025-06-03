package me.xjqsh.lrtactical.item.throwable.area;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

// 区域云投掷物属性配置
public class EffectCloudThrowableData extends ThrowableData {
    @SerializedName("cloud")
    private CloudData explode = new CloudData();

    @NotNull
    public CloudData getCloudData() {
        return explode;
    }

    public static class CloudData {
        @SerializedName("radius")
        private float radius = 5.5f;

        @SerializedName("radius_per_tick")
        private float radiusPerTick = 0.01f;

        @SerializedName("wait_time")
        private int waitTime = 20;

        @SerializedName("duration")
        private int duration = 200;

        @SerializedName("particles")
        private ParticleOptions particles = ParticleTypes.EFFECT;

        @SerializedName("ignite")
        private boolean ignite = false;

        @SerializedName("extinguish_by_smoke")
        private boolean extinguishBySmoke = false;

        @SerializedName("effects")
        private List<EffectData> effects = Lists.newArrayList();

        public CloudData() {}

        public CloudData(float radius, float radiusPerTick, int waitTime, int duration, ParticleOptions particles,
                         boolean ignite, boolean extinguishBySmoke, List<EffectData> effects) {
            this.radius = radius;
            this.radiusPerTick = radiusPerTick;
            this.waitTime = waitTime;
            this.duration = duration;
            this.particles = particles;
            this.ignite = ignite;
            this.extinguishBySmoke = extinguishBySmoke;
            this.effects = effects;
        }

        public float getRadius() {
            return radius;
        }

        public float getRadiusPerTick() {
            return radiusPerTick;
        }

        public int getWaitTime() {
            return waitTime;
        }

        public int getDuration() {
            return duration;
        }

        public ParticleOptions getParticles() {
            return particles;
        }

        public boolean isIgnite() {
            return ignite;
        }

        public boolean isExtinguishBySmoke() {
            return extinguishBySmoke;
        }

        public List<EffectData> getEffects() {
            return effects;
        }

        public List<MobEffectInstance> getEffectInstances() {
            List<MobEffectInstance> instances = Lists.newArrayList();
            for (EffectData effect : effects) {
                instances.add(effect.toInstance());
            }
            return instances;
        }
    }

    public record EffectData(
        MobEffect type,
        int duration,
        int amplifier,
        boolean visible,
        boolean showIcon
    ) {
        public MobEffectInstance toInstance() {
            return new MobEffectInstance(type, duration, amplifier, false, visible, showIcon);
        }
    }

    public static class EffectDataDeSerializer implements JsonDeserializer<EffectData> {
        @Override
        public EffectData deserialize(JsonElement ele, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            if (ele.isJsonObject()) {
                var obj = ele.getAsJsonObject();
                ResourceLocation id = ctx.deserialize(obj.get("type"), ResourceLocation.class);
                MobEffect type1 = ForgeRegistries.MOB_EFFECTS.getValue(id);
                if (type1 == null) {
                    throw new JsonParseException("Unknown effect type: " + id);
                }
                int duration = GsonHelper.getAsInt(obj, "duration", 200);
                if (duration < 0) {
                    throw new JsonParseException("Duration must be non-negative: " + duration);
                }
                int amplifier = GsonHelper.getAsInt(obj, "amplifier", 0);
                if (amplifier < 0) {
                    throw new JsonParseException("Amplifier must be non-negative: " + amplifier);
                }
                boolean visible = GsonHelper.getAsBoolean(obj, "visible", true);
                boolean showIcon = GsonHelper.getAsBoolean(obj, "show_icon", true);
                return new EffectData(type1, duration, amplifier, visible, showIcon);
            }
            throw new JsonParseException("Invalid EffectData JSON: " + ele);
        }
    }
}
