package me.xjqsh.lrtactical.client.audio;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ICustomSoundSupplier {
    String FEEDBACK_SUFFIX = "_feedback";

    Map<String, ResourceLocation> getSounds();

    default ResourceLocation getSound(String key) {
        ResourceLocation sound = getSounds().get(key);
        if (sound == null && key.endsWith(FEEDBACK_SUFFIX)) {
            sound = getSounds().get(key.substring(0, key.length() - FEEDBACK_SUFFIX.length()));
        }
        return sound;
    }
}
