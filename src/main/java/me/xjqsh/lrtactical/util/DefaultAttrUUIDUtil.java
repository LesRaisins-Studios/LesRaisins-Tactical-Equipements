package me.xjqsh.lrtactical.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class DefaultAttrUUIDUtil {
    public static ResourceLocation getId(ResourceLocation attribute) {
        return switch (attribute.toString()) {
            case "minecraft:generic.attack_damage" -> Item.BASE_ATTACK_DAMAGE_ID;
            case "minecraft:generic.attack_speed" -> Item.BASE_ATTACK_SPEED_ID;
            default ->
                    ResourceLocation.fromNamespaceAndPath(
                            "lrtactical",
                            "attribute/" + attribute.getNamespace() + "/" + attribute.getPath());
        };
    }
}
