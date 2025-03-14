package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.init.ModEffects;
import me.xjqsh.lrtactical.item.throwable.flash.StunThrowableData;
import me.xjqsh.lrtactical.util.CustomExplosion;
import me.xjqsh.lrtactical.util.ParticleUtil;
import me.xjqsh.lrtactical.util.SightTraceUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;

public class StunGrenadeEntity extends ThrowableItemEntity {
    public static EntityType<StunGrenadeEntity> TYPE = EntityType.Builder.<StunGrenadeEntity>of(StunGrenadeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .setCustomClientFactory(StunGrenadeEntity::new)
            .sized(0.3f, 0.3f)
            .noSave()
            .noSummon()
            .fireImmune()
            .build("grenade_entity");

    private StunThrowableData.StunData data = new StunThrowableData.StunData();

    public StunGrenadeEntity(LivingEntity entity, Level level, int lifeTime) {
        super(TYPE, entity, level, lifeTime);
    }

    public StunGrenadeEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        super(TYPE, level);
    }

    public StunGrenadeEntity(EntityType<StunGrenadeEntity> type, Level level) {
        super(type, level);
    }


    public StunThrowableData.StunData getData() {
        return data;
    }

    public void setData(StunThrowableData.StunData data) {
        this.data = data;
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
            double radius = this.getData().getRadius();
            AABB aabb = this.getBoundingBox().inflate(radius);
            for (Entity entity : this.level().getEntities(this, aabb, EntitySelector.NO_SPECTATORS)) {
                if (entity instanceof LivingEntity living) {
                    calculateAndApplyEffect(this, living, this.getData());
                }
            }
        }
        super.onDeath();
    }

    public static void calculateAndApplyEffect(Entity starter, LivingEntity target, StunThrowableData.StunData data) {
        // origin position
        Vec3 p = starter.position().add(0.0,1.0,0.0);
        // target eyes
        Vec3 eyes = target.getEyePosition(1.0F);
        // f to t
        Vec3 d1 = p.subtract(eyes);

        double distanceMax = data.getRadius();
        double distance = d1.length();

        if(distance > distanceMax){
            return;
        }

        // Calculate angle between eye-gaze line and eye-grenade line
        // 目标视线与两点连线的夹角
        double a1 = Math.toDegrees(Math.acos(target.getViewVector(1.0F).dot(d1.normalize())));
        // 目标的视线范围
        double angleMax = data.getBlind().getMaxAngle();

        if(a1 > 0 && a1 < angleMax){
            if(SightTraceUtil.rayTraceOpaqueBlocks(starter, target.level(), eyes, p, false, false, false) == null) {
                // Duration attenuated by distance
                int durationBlinded = data.calcBlindDuration(distance, a1);
                target.addEffect(new MobEffectInstance(ModEffects.BLIND.get(), durationBlinded));
            }
        }

        int durationDeafened = data.calcDeafenedDuration(distance);
        target.addEffect(new MobEffectInstance(ModEffects.DEAFENED.get(), durationDeafened));
    }
}
