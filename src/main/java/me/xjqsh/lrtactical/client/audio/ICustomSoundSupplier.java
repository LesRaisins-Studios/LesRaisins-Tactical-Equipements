package me.xjqsh.lrtactical.client.audio;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ICustomSoundSupplier {
    Map<String, ResourceLocation> getSounds();

    default ResourceLocation getSound(String key) {
        return getSounds().get(key);
    }
}
