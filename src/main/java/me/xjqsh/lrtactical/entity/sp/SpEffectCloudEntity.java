package me.xjqsh.lrtactical.entity.sp;

import me.xjqsh.lrtactical.entity.SmokeGrenadeEntity;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpEffectCloudEntity extends AreaEffectCloud {
    public static EntityType<SpEffectCloudEntity> TYPE = EntityType.Builder.<SpEffectCloudEntity>of(SpEffectCloudEntity::new, MobCategory.MISC)
            .fireImmune()
            .sized(6.0F, 0.5F)
            .noSave()
            .noSummon()
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .build("sp_effect_cloud");

    private boolean ignite = false;
    private boolean extinguishBySmoke = false;

    public SpEffectCloudEntity(EntityType<? extends AreaEffectCloud> type, Level level) {
        super(type, level);
    }

    public SpEffectCloudEntity(Level level, double x, double y, double z) {
        this(TYPE, level);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.isIgnite() && tickCount % 10 == 0){
            List<Entity> list1 = this.level().getEntitiesOfClass(Entity.class, this.getBoundingBox().inflate(0, 2, 0));
            for (Entity entity : list1) {
                if (this.isExtinguishBySmoke() && entity instanceof SmokeGrenadeEntity smokeGrenade) {
                    if (shouldBeExtinguished(smokeGrenade)) {
                        this.discard();
                    }
                    continue;
                }

                if (entity instanceof LivingEntity && this.getBoundingBox().intersects(entity.getBoundingBox())) {
                    if (!entity.fireImmune()) {
                        entity.setSecondsOnFire(2);
                    }
                }
            }
        }
    }

    public boolean shouldBeExtinguished(SmokeGrenadeEntity smokeGrenade) {
        return smokeGrenade.tickCount >= 40 && smokeGrenade.position().distanceToSqr(this.position()) < 25;
    }

    @NotNull
    public EntityDimensions getDimensions(@NotNull Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
    }

    public boolean isIgnite() {
        return ignite;
    }

    public void setIgnite(boolean ignite) {
        this.ignite = ignite;
    }

    public boolean isExtinguishBySmoke() {
        return extinguishBySmoke;
    }

    public void setExtinguishBySmoke(boolean extinguishBySmoke) {
        this.extinguishBySmoke = extinguishBySmoke;
    }
}
