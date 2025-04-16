package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponType;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistries {
    public static final ResourceKey<Registry<ThrowableType<?, ?>>> THROWABLE_TYPE = ResourceKey.createRegistryKey(
            new ResourceLocation(EquipmentMod.MOD_ID, "throwable_type")
    );
    public static Supplier<IForgeRegistry<ThrowableType<?, ?>>> THROWABLE_TYPE_SUPPLIER;

    public static final ResourceKey<Registry<MeleeWeaponType<?>>> MELEE_WEAPON_TYPE = ResourceKey.createRegistryKey(
            new ResourceLocation(EquipmentMod.MOD_ID, "melee_type")
    );
    public static Supplier<IForgeRegistry<MeleeWeaponType<?>>> MELEE_WEAPON_TYPE_SUPPLIER;

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        THROWABLE_TYPE_SUPPLIER = event.create(new RegistryBuilder<ThrowableType<?, ?>>().setName(THROWABLE_TYPE.location()));
        MELEE_WEAPON_TYPE_SUPPLIER = event.create(new RegistryBuilder<MeleeWeaponType<?>>().setName(MELEE_WEAPON_TYPE.location()));
    }
}
