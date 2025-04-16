package me.xjqsh.lrtactical.util;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.UUID;

public class DefaultAttrUUIDUtil {
    private static final Map<ResourceLocation, UUID> cache = Maps.newHashMap();
    // 这俩有特殊作用
    static {
        cache.put(new ResourceLocation("minecraft:generic.attack_damage"), Item.BASE_ATTACK_DAMAGE_UUID);
        cache.put(new ResourceLocation("minecraft:generic.attack_speed"), Item.BASE_ATTACK_SPEED_UUID);
    }

    public static UUID getUUID(ResourceLocation id) {
        return cache.computeIfAbsent(id, k -> UUID.randomUUID());
    }
}
