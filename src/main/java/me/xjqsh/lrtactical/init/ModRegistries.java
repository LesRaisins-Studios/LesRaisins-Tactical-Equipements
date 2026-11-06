package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponType;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModRegistries {
    public static final ResourceKey<Registry<ThrowableType<?, ?>>> THROWABLE_TYPE =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "throwable_type"));
    public static Registry<ThrowableType<?, ?>> THROWABLE_TYPE_REGISTRY;

    public static final ResourceKey<Registry<MeleeWeaponType<?>>> MELEE_WEAPON_TYPE =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "melee_type"));
    public static Registry<MeleeWeaponType<?>> MELEE_WEAPON_TYPE_REGISTRY;

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        THROWABLE_TYPE_REGISTRY = new RegistryBuilder<ThrowableType<?, ?>>(THROWABLE_TYPE).create();
        event.register(THROWABLE_TYPE_REGISTRY);

        MELEE_WEAPON_TYPE_REGISTRY =
                new RegistryBuilder<MeleeWeaponType<?>>(MELEE_WEAPON_TYPE).create();
        event.register(MELEE_WEAPON_TYPE_REGISTRY);
    }
}
