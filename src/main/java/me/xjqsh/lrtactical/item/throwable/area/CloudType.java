package me.xjqsh.lrtactical.item.throwable.area;

import me.xjqsh.lrtactical.config.ServerConfig;
import me.xjqsh.lrtactical.entity.EffectCloudGrenadeEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CloudType {
    public static final ThrowableType<EffectCloudThrowableData, EffectCloudGrenadeEntity> CLOUD = ThrowableType.Builder.<EffectCloudThrowableData, EffectCloudGrenadeEntity>of()
            .setFactory(CloudType::createEntity)
            .setSerializer((jsonElement) -> CommonAssetsManager.GSON.fromJson(jsonElement, EffectCloudThrowableData.class))
            .build();

    public static EffectCloudGrenadeEntity createEntity(ItemStack stack, LivingEntity thrower, EffectCloudThrowableData data) {
        var entity = new EffectCloudGrenadeEntity(thrower, thrower.level(), data.getEntityData().getLifeTime());
        float initialSpeed = (float) data.getInitialSpeed();
        if (thrower.isCrouching()) {
            initialSpeed *= ServerConfig.CROUCHING_INIT_SPEED_PERCENT.get(); // Reduce speed if crouching
        }
        entity.shootFromRotation(entity, thrower.getXRot(), thrower.getYRot(), 0.0F, initialSpeed, 1.0F);
        entity.setItem(stack);

        entity.setGravity(data.getEntityData().getGravity());
        entity.setBounceFactor(data.getEntityData().getBounceFactor());
        entity.setShouldBounce(data.getEntityData().isShouldBounce());

        entity.setCloudData(data.getCloudData());

        return entity;
    }
}
