package me.xjqsh.lrtactical.item.throwable.smoke;

import me.xjqsh.lrtactical.entity.SmokeGrenadeEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SmokeType {
    public static final ThrowableType<ThrowableData, SmokeGrenadeEntity> SMOKE = ThrowableType.Builder.<ThrowableData, SmokeGrenadeEntity>of()
            .setFactory(SmokeType::createEntity)
            .setSerializer((jsonElement) -> CommonAssetsManager.GSON.fromJson(jsonElement, ThrowableData.class))
            .build();

    public static SmokeGrenadeEntity createEntity(ItemStack stack, LivingEntity thrower, ThrowableData data) {
        var entity = new SmokeGrenadeEntity(thrower, thrower.level(), data.getEntityData().getLifeTime());
        float initialSpeed = (float) data.getInitialSpeed();
        entity.shootFromRotation(entity, thrower.getXRot(), thrower.getYRot(), 0.0F, initialSpeed, 1.0F);
        entity.setItem(stack);

        entity.setGravity(data.getEntityData().getGravity());
        entity.setBounceFactor(data.getEntityData().getBounceFactor());
        entity.setShouldBounce(data.getEntityData().isShouldBounce());

        return entity;
    }
}
