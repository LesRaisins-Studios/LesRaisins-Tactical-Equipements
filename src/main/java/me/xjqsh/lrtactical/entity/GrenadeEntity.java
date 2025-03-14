package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.util.CustomExplosion;
import me.xjqsh.lrtactical.util.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
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
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.1, this.getZ(), 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public void onDeath() {
        if (!this.level().isClientSide()) {
            CustomExplosion explosion = new CustomExplosion(this.level(), this, this.getDamage(), this.getRadius(), Explosion.BlockInteraction.KEEP);
            explosion.explode();
            net.minecraftforge.event.ForgeEventFactory.onExplosionStart(level(), explosion);
            explosion.finalizeExplosion(true);
            if (this.level() instanceof ServerLevel level) {
                double x = this.getX();
                double y = this.getY();
                double z = this.getZ();
                ParticleUtil.sendParticle(level, ParticleTypes.FLASH, x, y + 0.5, z, 50, 0.2, 0.2, 0.2, 20, true);
                ParticleUtil.sendParticle(level, ParticleTypes.EXPLOSION_EMITTER, x, y + 1, z, 5, 0.7, 0.7, 0.7, 1, true);
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 6.0f, 1.0f);
            }
        }
        super.onDeath();
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
}
