package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.init.ModParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;

public class SmokeGrenadeEntity extends ThrowableItemEntity {
    public static EntityType<SmokeGrenadeEntity> TYPE = EntityType.Builder.<SmokeGrenadeEntity>of(SmokeGrenadeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .setCustomClientFactory(SmokeGrenadeEntity::new)
            .sized(0.3f, 0.3f)
            .noSave()
            .noSummon()
            .fireImmune()
            .build("smoke_grenade_entity");

    public SmokeGrenadeEntity(LivingEntity entity, Level level, int lifeTime) {
        super(TYPE, entity, level, lifeTime);
    }

    public SmokeGrenadeEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        super(TYPE, level);
    }

    public SmokeGrenadeEntity(EntityType<SmokeGrenadeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.tickCount >= 40) {
                // 在半球形范围内生成烟雾粒子
                double x = this.getX();
                double y = this.getY();
                double z = this.getZ();
                for (int i = 0; i < 16; i++) {
                    double offsetX = this.random.triangle(0, 5.5);
                    double offsetY = this.random.triangle(0, 4.5);
                    double offsetZ = this.random.triangle(0, 5.5);
                    this.level().addParticle(ModParticleTypes.SMOKE_CLOUD.get(), true, x + offsetX, y + offsetY, z + offsetZ, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

}
