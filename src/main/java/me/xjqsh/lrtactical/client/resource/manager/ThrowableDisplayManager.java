package me.xjqsh.lrtactical.client.resource.manager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.manager.JsonDataManager;
import me.xjqsh.lrtactical.client.resource.display.ThrowableDisplayInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ThrowableDisplayManager extends JsonDataManager<ThrowableDisplayInstance> {
    public ThrowableDisplayManager(Gson pGson) {
        super(null, pGson, "display/throwable", "ThrowableDisplay");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        dataMap.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                var pojo = getGson().fromJson(element, ThrowableDisplayInstance.ThrowableDisplay.class);
                ThrowableDisplayInstance data = ThrowableDisplayInstance.create(pojo, id);
                dataMap.put(id, data);
            } catch (JsonParseException | IllegalArgumentException e) {
                GunMod.LOGGER.error(getMarker(), "Failed to load display file {}", id, e);
            }
        }
    }
}
