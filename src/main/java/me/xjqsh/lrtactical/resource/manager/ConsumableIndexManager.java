package me.xjqsh.lrtactical.resource.manager;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.manager.JsonDataManager;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class ConsumableIndexManager extends JsonDataManager<ConsumableIndex> {
    public ConsumableIndexManager(Gson pGson) {
        super(null, pGson, "index/consumable", "ConsumableIndex");
    }

    private Map<ResourceLocation, String> networkCache = new HashMap<>();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        dataMap.clear();
        ImmutableMap.Builder<ResourceLocation, String> builder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) {
                GunMod.LOGGER.error(getMarker(), "Failed to load index file {}: Expected object, got {} ", id, element);
                continue;
            }
            JsonObject pJson = element.getAsJsonObject();

            try {
                ConsumableIndex index = parse(pJson, id);
                dataMap.put(id, index);
                builder.put(id, pJson.toString());
            } catch (JsonParseException | IllegalArgumentException e) {
                GunMod.LOGGER.error(getMarker(), "Failed to load index file {}", id, e);
            }
        }

        this.networkCache = builder.build();
    }

    public Map<ResourceLocation, String> getCache() {
        return networkCache;
    }

    public static ConsumableIndex parse(JsonObject pJson, ResourceLocation id) throws JsonParseException {
        String name = GsonHelper.getAsString(pJson, "name", "unknown.lrtactical.name");

        String baseItem = GsonHelper.getAsString(pJson, "base_item", "lrtactical:consumable");
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(baseItem));
        if (item == null) {
            throw new JsonParseException("Unknown item id \"" + baseItem + "\"");
        }

        JsonObject data = GsonHelper.getAsJsonObject(pJson, "data");

        return ConsumableIndex.deserialize(data, name, id, item);
    }
}
