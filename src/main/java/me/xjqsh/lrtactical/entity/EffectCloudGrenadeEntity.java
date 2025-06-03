package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.entity.sp.SpEffectCloudEntity;
import me.xjqsh.lrtactical.item.throwable.area.EffectCloudThrowableData;
import me.xjqsh.lrtactical.util.CustomExplosion;
import me.xjqsh.lrtactical.util.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PlayMessages;

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
    public void onDeath() {
        if (!this.level().isClientSide() && this.cloudData != null) {
            var cloud = new SpEffectCloudEntity(this.level(), this.getX(), this.getY(), this.getZ());
            var cloudData = this.getCloudData();

            cloud.setRadius(cloudData.getRadius());
            cloud.setRadiusPerTick(cloudData.getRadiusPerTick());
            cloud.setDuration(cloudData.getDuration());
            cloud.setWaitTime(cloudData.getWaitTime());
            cloud.setParticle(cloudData.getParticles());
            cloud.setIgnite(cloudData.isIgnite());
            for (var effect : cloudData.getEffects()) {
                cloud.addEffect(effect.toInstance());
            }

            if (this.getOwner() instanceof LivingEntity livingEntity) {
                cloud.setOwner(livingEntity);
            }

            this.level().addFreshEntity(cloud);
        }
        super.onDeath();
    }

    public EffectCloudThrowableData.CloudData getCloudData() {
        return cloudData;
    }

    public void setCloudData(EffectCloudThrowableData.CloudData cloudData) {
        this.cloudData = cloudData;
    }
}
