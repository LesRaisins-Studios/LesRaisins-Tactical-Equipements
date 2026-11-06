package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, EquipmentMod.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKE_CLOUD =

            PARTICLE_TYPES.register("smoke_cloud", () -> new SimpleParticleType(true));
}

