package me.xjqsh.lrtactical.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import me.xjqsh.lrtactical.network.DataType;
import me.xjqsh.lrtactical.resource.manager.ConsumableIndexManager;
import me.xjqsh.lrtactical.resource.manager.MeleeIndexManager;
import me.xjqsh.lrtactical.resource.manager.ThrowableIndexManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;

public enum CommonNetworkCache implements ICommonResourceProvider {
    INSTANCE;

    public Map<ResourceLocation, ConsumableIndex> consumableIndex = Maps.newHashMap();
    public Map<ResourceLocation, ThrowableIndex<?, ?>> throwableIndex = Maps.newHashMap();
    public Map<ResourceLocation, MeleeWeaponIndex<?>> meleeWeaponIndex = Maps.newHashMap();

    @Override
    public ConsumableIndex getConsumableIndex(ResourceLocation id) {
        return consumableIndex.get(id);
    }

    @Override
    public Collection<ConsumableIndex> getConsumableIndexes() {
        return consumableIndex.values();
    }

    @Override
    public ThrowableIndex<?, ?> getThrowableIndex(ResourceLocation id) {
        return throwableIndex.get(id);
    }

    @Override
    public Collection<ThrowableIndex<?,?>> getThrowableIndexes() {
        return throwableIndex.values();
    }

    @Override
    public MeleeWeaponIndex<?> getMeleeIndex(ResourceLocation id) {
        return meleeWeaponIndex.get(id);
    }

    @Override
    public Collection<MeleeWeaponIndex<?>> getMeleeIndexes() {
        return meleeWeaponIndex.values();
    }

    public void parseConsumableIndex(Map<ResourceLocation, String> cache) {
        ImmutableMap.Builder<ResourceLocation, ConsumableIndex> builder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, String> consumableEntry : cache.entrySet()) {
            JsonObject jsonObject = CommonAssetsManager.GSON.fromJson(consumableEntry.getValue(), JsonObject.class);
            var index = ConsumableIndexManager.parse(jsonObject, consumableEntry.getKey());
            if (index != null) {
                builder.put(consumableEntry.getKey(), index);
            }
        }
        this.consumableIndex = builder.build();
    }

    public void parseThrowableIndex(Map<ResourceLocation, String> cache) {
        ImmutableMap.Builder<ResourceLocation, ThrowableIndex<?, ?>> builder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, String> throwableEntry : cache.entrySet()) {
            JsonObject jsonObject = CommonAssetsManager.GSON.fromJson(throwableEntry.getValue(), JsonObject.class);
            var index = ThrowableIndexManager.parse(jsonObject, throwableEntry.getKey());
            if (index != null) {
                builder.put(throwableEntry.getKey(), index);
            }
        }
        this.throwableIndex = builder.build();
    }

    public void parseMeleeIndex(Map<ResourceLocation, String> cache) {
        ImmutableMap.Builder<ResourceLocation, MeleeWeaponIndex<?>> builder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, String> meleeEntry : cache.entrySet()) {
            JsonObject jsonObject = CommonAssetsManager.GSON.fromJson(meleeEntry.getValue(), JsonObject.class);
            var index = MeleeIndexManager.parse(jsonObject, meleeEntry.getKey());
            if (index != null) {
                builder.put(meleeEntry.getKey(), index);
            }
        }
        this.meleeWeaponIndex = builder.build();
    }

    public void fromNetwork(Map<DataType, Map<ResourceLocation, String>> cache) {
        for (Map.Entry<DataType, Map<ResourceLocation, String>> entry : cache.entrySet()) {
            switch (entry.getKey()) {
                case CONSUMABLE_INDEX -> parseConsumableIndex(entry.getValue());
                case THROWABLE_INDEX -> parseThrowableIndex(entry.getValue());
                case MELEE_INDEX -> parseMeleeIndex(entry.getValue());
                default -> {
                    // ignore
                }
            }
        }
    }
}
