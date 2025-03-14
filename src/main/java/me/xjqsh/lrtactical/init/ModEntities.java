package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.entity.GrenadeEntity;
import me.xjqsh.lrtactical.entity.SmokeGrenadeEntity;
import me.xjqsh.lrtactical.entity.StunGrenadeEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EquipmentMod.MOD_ID);

    public static RegistryObject<EntityType<GrenadeEntity>> GRENADE = ENTITY_TYPES.register("explode_grenade", () -> GrenadeEntity.TYPE);
    public static RegistryObject<EntityType<SmokeGrenadeEntity>> SMOKE_GRENADE = ENTITY_TYPES.register("smoke_grenade", () -> SmokeGrenadeEntity.TYPE);
    public static RegistryObject<EntityType<StunGrenadeEntity>> STUN_GRENADE = ENTITY_TYPES.register("stun_grenade", () -> StunGrenadeEntity.TYPE);
}