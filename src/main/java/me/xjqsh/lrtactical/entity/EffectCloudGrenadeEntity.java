package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.entity.sp.SpEffectCloudEntity;
import me.xjqsh.lrtactical.item.throwable.area.EffectCloudThrowableData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;

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
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.1, this.getZ(), 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public void onDeath(@Nullable HitResult hitResult) {
        Vec3 pos = hitResult == null ? this.position() : hitResult.getLocation();

        if (!this.level().isClientSide() && this.cloudData != null) {
            var cloud = new SpEffectCloudEntity(this.level(), pos.x(), pos.y(), pos.z());
            var cloudData = this.getCloudData();

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
        super.onDeath(hitResult);
    }

    public EffectCloudThrowableData.CloudData getCloudData() {
        return cloudData;
    }

    public void setCloudData(EffectCloudThrowableData.CloudData cloudData) {
        this.cloudData = cloudData;
    }
}
