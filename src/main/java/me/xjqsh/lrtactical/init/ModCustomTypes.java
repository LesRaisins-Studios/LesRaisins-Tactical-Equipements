package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.entity.*;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponData;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponType;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.item.throwable.area.CloudType;
import me.xjqsh.lrtactical.item.throwable.area.EffectCloudThrowableData;
import me.xjqsh.lrtactical.item.throwable.explode.ExplodeThrowableData;
import me.xjqsh.lrtactical.item.throwable.explode.ExplodeType;
import me.xjqsh.lrtactical.item.throwable.explode.StickyType;
import me.xjqsh.lrtactical.item.throwable.flash.StunThrowableData;
import me.xjqsh.lrtactical.item.throwable.flash.StunType;
import me.xjqsh.lrtactical.item.throwable.smoke.SmokeType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCustomTypes {
    // 投掷物类型
    public static final DeferredRegister<ThrowableType<?, ?>> THROWABLE_TYPES = DeferredRegister
            .create(ModRegistries.THROWABLE_TYPE, EquipmentMod.MOD_ID);

    public static DeferredHolder<ThrowableType<?, ?>, ThrowableType<ExplodeThrowableData, GrenadeEntity>> EXPLODE = THROWABLE_TYPES.register("explode",
            () -> ExplodeType.EXPLODE
    );

    public static DeferredHolder<ThrowableType<?, ?>, ThrowableType<ExplodeThrowableData, StickyGrenadeEntity>> STICKY = THROWABLE_TYPES.register("sticky",
            () -> StickyType.STICKY
    );

    public static DeferredHolder<ThrowableType<?, ?>, ThrowableType<ThrowableData, SmokeGrenadeEntity>> SMOKE = THROWABLE_TYPES.register("smoke",
            () -> SmokeType.SMOKE
    );

    public static DeferredHolder<ThrowableType<?, ?>, ThrowableType<StunThrowableData, StunGrenadeEntity>> STUN = THROWABLE_TYPES.register("stun",
            () -> StunType.STUN
    );

    public static DeferredHolder<ThrowableType<?, ?>, ThrowableType<EffectCloudThrowableData, EffectCloudGrenadeEntity>> EFFECT_CLOUD = THROWABLE_TYPES.register("effect_cloud",
            () -> CloudType.CLOUD
    );

    // 近战武器
    public static final DeferredRegister<MeleeWeaponType<?>> MELEE_WEAPON_TYPES = DeferredRegister
            .create(ModRegistries.MELEE_WEAPON_TYPE, EquipmentMod.MOD_ID);

    public static DeferredHolder<MeleeWeaponType<?>, MeleeWeaponType<MeleeWeaponData>> NORMAL = MELEE_WEAPON_TYPES.register("normal",
            () -> new MeleeWeaponType<>(
                    (jsonElement) -> CommonAssetsManager.GSON.fromJson(jsonElement, MeleeWeaponData.class)
            )
    );
}
