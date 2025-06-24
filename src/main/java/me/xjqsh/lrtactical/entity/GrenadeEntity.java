package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.config.CommonConfig;
import me.xjqsh.lrtactical.util.CustomExplosion;
import me.xjqsh.lrtactical.util.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PlayMessages;

public class GrenadeEntity extends ThrowableItemEntity {
    public static EntityType<GrenadeEntity> TYPE = EntityType.Builder.<GrenadeEntity>of(GrenadeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .setCustomClientFactory(GrenadeEntity::new)
            .sized(0.3f, 0.3f)
            .noSave()
            .noSummon()
            .fireImmune()
            .build("grenade_entity");

    private double damage = 18.0;
    private float radius = 4.5f;
    private boolean destroyBlocks = false;
    private double screenShakeTime = 20;
    private double screenShakeAmplitude = 50;

    public GrenadeEntity(LivingEntity entity, Level level, int lifeTime) {
        super(TYPE, entity, level, lifeTime);
    }

    public GrenadeEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        super(TYPE, level);
    }

    public GrenadeEntity(EntityType<GrenadeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void onDeath(HitResult hitResult) {
        Vec3 pos = hitResult == null ? this.position() : this.position().lerp(hitResult.getLocation(), 0.8);
        if (!this.level().isClientSide()) {
            var type = this.isDestroyBlocks() && CommonConfig.GRENADE_EXPLOSION_BLOCK_DAMAGE.get() ?
                    Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
            CustomExplosion explosion = new CustomExplosion(this.level(), this, this.getDamage(), this.getRadius(), type);
            explosion.setScreenShakeAmplitude(this.screenShakeAmplitude);
            explosion.setScreenShakeTime(this.screenShakeTime);
            if (ForgeEventFactory.onExplosionStart(level(), explosion)) {
                return;
            }
            explosion.explode();
            explosion.finalizeExplosion(true);
            if (this.level() instanceof ServerLevel level) {
                double x = pos.x();
                double y = pos.y();
                double z = pos.z();
                ParticleUtil.sendParticle(level, ParticleTypes.FLASH, x, y + 0.5, z, 50, 0.2, 0.2, 0.2, 20, true);
                ParticleUtil.sendParticle(level, ParticleTypes.EXPLOSION_EMITTER, x, y + 1, z, 5, 0.7, 0.7, 0.7, 1, true);
            }
        }
        super.onDeath(hitResult);
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isDestroyBlocks() {
        return destroyBlocks;
    }

    public void setDestroyBlocks(boolean destroyBlocks) {
        this.destroyBlocks = destroyBlocks;
    }

    public double getScreenShakeTime() {
        return screenShakeTime;
    }

    public void setScreenShakeTime(double screenShakeTime) {
        this.screenShakeTime = screenShakeTime;
    }

    public double getScreenShakeAmplitude() {
        return screenShakeAmplitude;
    }

    public void setScreenShakeAmplitude(double screenShakeAmplitude) {
        this.screenShakeAmplitude = screenShakeAmplitude;
    }
}
