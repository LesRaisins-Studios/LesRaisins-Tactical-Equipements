package me.xjqsh.lrtactical.item.throwable.explode;

import me.xjqsh.lrtactical.entity.GrenadeEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ExplodeType {
    public static final ThrowableType<ExplodeThrowableData, GrenadeEntity> EXPLODE = ThrowableType.Builder.<ExplodeThrowableData, GrenadeEntity>of()
            .setFactory(ExplodeType::createEntity)
            .setSerializer((jsonElement) -> CommonAssetsManager.GSON.fromJson(jsonElement, ExplodeThrowableData.class))
            .build();

    public static GrenadeEntity createEntity(ItemStack stack, LivingEntity thrower, ExplodeThrowableData data) {
        var entity = new GrenadeEntity(thrower, thrower.level(), data.getEntityData().getLifeTime());
        float initialSpeed = (float) data.getInitialSpeed();
        entity.shootFromRotation(entity, thrower.getXRot(), thrower.getYRot(), 0.0F, initialSpeed, 1.0F);
        entity.setItem(stack);

        entity.setGravity(data.getEntityData().getGravity());
        entity.setBounceFactor(data.getEntityData().getBounceFactor());
        entity.setShouldBounce(data.getEntityData().isShouldBounce());

        entity.setDamage(data.getExplode().getDamage());
        entity.setRadius(data.getExplode().getRadius());

        return entity;
    }
}
