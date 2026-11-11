package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.effect.BurnedEffect;
import me.xjqsh.lrtactical.effect.HarmfulEffect;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, EquipmentMod.MOD_ID);

    public static final DeferredHolder<MobEffect, HarmfulEffect> BLIND = EFFECTS.register("blinded", () -> new HarmfulEffect(0xffffff));
    public static final DeferredHolder<MobEffect, HarmfulEffect> DEAFENED = EFFECTS.register("deafened", () -> new HarmfulEffect(0xffffff));
    public static final DeferredHolder<MobEffect, BurnedEffect> FLAMMABLE = EFFECTS.register("flammable", () -> new BurnedEffect(0xaa4727));
}
