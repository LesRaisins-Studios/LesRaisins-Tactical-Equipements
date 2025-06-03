package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.entity.EffectCloudGrenadeEntity;
import me.xjqsh.lrtactical.entity.GrenadeEntity;
import me.xjqsh.lrtactical.entity.SmokeGrenadeEntity;
import me.xjqsh.lrtactical.entity.StunGrenadeEntity;
import me.xjqsh.lrtactical.entity.sp.SpEffectCloudEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EquipmentMod.MOD_ID);

    public static RegistryObject<EntityType<GrenadeEntity>> GRENADE = ENTITY_TYPES.register("explode_grenade", () -> GrenadeEntity.TYPE);
    public static RegistryObject<EntityType<SmokeGrenadeEntity>> SMOKE_GRENADE = ENTITY_TYPES.register("smoke_grenade", () -> SmokeGrenadeEntity.TYPE);
    public static RegistryObject<EntityType<StunGrenadeEntity>> STUN_GRENADE = ENTITY_TYPES.register("stun_grenade", () -> StunGrenadeEntity.TYPE);
    public static RegistryObject<EntityType<EffectCloudGrenadeEntity>> EFFECT_GRENADE = ENTITY_TYPES.register("effect_grenade", () -> EffectCloudGrenadeEntity.TYPE);

    public static RegistryObject<EntityType<SpEffectCloudEntity>> EFFECT_CLOUD = ENTITY_TYPES.register("sp_effect_cloud", () -> SpEffectCloudEntity.TYPE);

}