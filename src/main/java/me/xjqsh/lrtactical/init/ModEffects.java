package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.effect.BurnedEffect;
import me.xjqsh.lrtactical.effect.HarmfulEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EquipmentMod.MOD_ID);

    public static final RegistryObject<HarmfulEffect> BLIND = EFFECTS.register("blinded", () -> new HarmfulEffect(0xffffff));
    public static final RegistryObject<HarmfulEffect> DEAFENED = EFFECTS.register("deafened", () -> new HarmfulEffect(0xffffff));
    public static final RegistryObject<BurnedEffect> FLAMMABLE = EFFECTS.register("flammable", () -> new BurnedEffect(0xaa4727));
}
