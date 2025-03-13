package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, EquipmentMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SMOKE_CLOUD = PARTICLE_TYPES.register("smoke_cloud", () -> new SimpleParticleType(true));
}

