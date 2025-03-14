package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.entity.GrenadeEntity;
import me.xjqsh.lrtactical.entity.SmokeGrenadeEntity;
import me.xjqsh.lrtactical.entity.StunGrenadeEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.item.throwable.explode.ExplodeThrowableData;
import me.xjqsh.lrtactical.item.throwable.explode.ExplodeType;
import me.xjqsh.lrtactical.item.throwable.flash.StunThrowableData;
import me.xjqsh.lrtactical.item.throwable.flash.StunType;
import me.xjqsh.lrtactical.item.throwable.smoke.SmokeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModThrowableTypes {
    public static final DeferredRegister<ThrowableType<?, ?>> THROWABLE_TYPES = DeferredRegister
            .create(ModRegistries.THROWABLE_TYPE, EquipmentMod.MOD_ID);

    public static RegistryObject<ThrowableType<ExplodeThrowableData, GrenadeEntity>> EXPLODE = THROWABLE_TYPES.register("explode",
            () -> ExplodeType.EXPLODE
    );

    public static RegistryObject<ThrowableType<ThrowableData, SmokeGrenadeEntity>> SMOKE = THROWABLE_TYPES.register("smoke",
            () -> SmokeType.SMOKE
    );

    public static RegistryObject<ThrowableType<StunThrowableData, StunGrenadeEntity>> STUN = THROWABLE_TYPES.register("stun",
            () -> StunType.STUN
    );
}
