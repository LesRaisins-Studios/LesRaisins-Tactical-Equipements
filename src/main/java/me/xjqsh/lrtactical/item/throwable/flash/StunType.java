package me.xjqsh.lrtactical.item.throwable.flash;

import me.xjqsh.lrtactical.config.ServerConfig;
import me.xjqsh.lrtactical.entity.StunGrenadeEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class StunType {
    public static final ThrowableType<StunThrowableData, StunGrenadeEntity> STUN = ThrowableType.Builder.<StunThrowableData, StunGrenadeEntity>of()
            .setFactory(StunType::createEntity)
            .setSerializer((jsonElement) -> CommonAssetsManager.GSON.fromJson(jsonElement, StunThrowableData.class))
            .build();

    public static StunGrenadeEntity createEntity(ItemStack stack, LivingEntity thrower, StunThrowableData data) {
        var entity = new StunGrenadeEntity(thrower, thrower.level(), data.getEntityData().getLifeTime());
        float initialSpeed = (float) data.getInitialSpeed();
        if (thrower.isCrouching()) {
            initialSpeed *= ServerConfig.CROUCHING_INIT_SPEED_PERCENT.get(); // Reduce speed if crouching
        }
        entity.shootFromRotation(entity, thrower.getXRot(), thrower.getYRot(), 0.0F, initialSpeed, 1.0F);
        entity.setItem(stack);

        entity.setBaseData(data.getEntityData());

        entity.setStunData(data.getStunData());

        return entity;
    }
}
