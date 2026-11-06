package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantment {
    public static final ResourceKey<Enchantment> BACKSTAB =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "backstab"));
}
