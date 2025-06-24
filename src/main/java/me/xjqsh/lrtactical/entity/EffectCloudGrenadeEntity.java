package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.entity.sp.SpEffectCloudEntity;
import me.xjqsh.lrtactical.item.throwable.area.EffectCloudThrowableData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EffectCloudGrenadeEntity extends ThrowableItemEntity {
    public static EntityType<EffectCloudGrenadeEntity> TYPE = EntityType.Builder.<EffectCloudGrenadeEntity>of(EffectCloudGrenadeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .setCustomClientFactory(EffectCloudGrenadeEntity::new)
            .sized(0.3f, 0.3f)
            .noSave()
            .noSummon()
            .fireImmune()
            .build("grenade_entity");

    private EffectCloudThrowableData.CloudData cloudData;

    public EffectCloudGrenadeEntity(LivingEntity entity, Level level, int lifeTime) {
        super(TYPE, entity, level, lifeTime);
    }

    public EffectCloudGrenadeEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        super(TYPE, level);
    }

    public EffectCloudGrenadeEntity(EntityType<EffectCloudGrenadeEntity> type, Level level) {
        super(type, level);
    }


    @Override
    public void onDeath(@Nullable HitResult hitResult) {
        Vec3 pos = hitResult == null ? this.position() : hitResult.getLocation();

        if (!this.level().isClientSide() && this.cloudData != null) {
            var cloudData = this.getCloudData();
            if (cloudData.isAreaCloud()) {
                spawnEffectCloud(pos, cloudData);
            } else {
                List<MobEffectInstance> effects = cloudData.getEffectInstances();
                Entity target = hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null;
                int color = PotionUtils.getColor(effects);

                applySplash(effects, cloudData.isIgnite(), cloudData.getIgniteTime(), target);
                this.level().levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, this.blockPosition(), color);
            }
        }
        super.onDeath(hitResult);
    }

    private void spawnEffectCloud(Vec3 pos, EffectCloudThrowableData.CloudData cloudData) {
        var cloud = new SpEffectCloudEntity(this.level(), pos.x(), pos.y(), pos.z());
        cloud.setRadius(cloudData.getRadius());
        cloud.setRadiusPerTick(cloudData.getRadiusPerTick());
        cloud.setDuration(cloudData.getDuration());
        cloud.setWaitTime(cloudData.getWaitTime());
        cloud.setParticle(cloudData.getParticles());
        cloud.setIgnite(cloudData.isIgnite());
        cloud.setExtinguishBySmoke(cloudData.isExtinguishBySmoke());
        for (var effect : cloudData.getEffects()) {
            cloud.addEffect(effect.toInstance());
        }

        if (this.getOwner() instanceof LivingEntity livingEntity) {
            cloud.setOwner(livingEntity);
        }

        this.level().addFreshEntity(cloud);
    }

    public void applySplash(List<MobEffectInstance> effectInstances, boolean ignite, int igniteTime, @Nullable Entity target) {
        AABB area = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);

        if (!entities.isEmpty()) {
            Entity source = this.getEffectSource();

            for (LivingEntity entity : entities) {
                if (!entity.isAffectedByPotions()) {
                    continue;
                }
                double distanceSqr = this.distanceToSqr(entity);
                if (distanceSqr < 16.0D) {
                    double d = (entity == target) ? 1.0D : 1.0D - Math.sqrt(distanceSqr) / 4.0D;
                    applyAllEffects(effectInstances, entity, d, source, ignite, igniteTime);
                }
            }
        }
    }

    public void applyAllEffects(List<MobEffectInstance> effectInstances, LivingEntity entity,
                                double d, Entity source, boolean ignite, int igniteTime) {
        for (MobEffectInstance effect : effectInstances) {
            MobEffect mobEffect = effect.getEffect();

            if (mobEffect.isInstantenous()) {
                mobEffect.applyInstantenousEffect(this, this.getOwner(), entity, effect.getAmplifier(), d);
            } else {
                int adjustedDuration = effect.mapDuration(duration -> (int) (d * duration + 0.5D));
                MobEffectInstance adjustedEffect = new MobEffectInstance(
                        mobEffect,
                        adjustedDuration,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible()
                );

                if (!adjustedEffect.endsWithin(20)) {
                    entity.addEffect(adjustedEffect, source);
                }
            }
        }
        if (ignite && !entity.fireImmune()) {
            entity.setSecondsOnFire(igniteTime);
        }
    }

    public EffectCloudThrowableData.CloudData getCloudData() {
        return cloudData;
    }

    public void setCloudData(EffectCloudThrowableData.CloudData cloudData) {
        this.cloudData = cloudData;
    }
}
