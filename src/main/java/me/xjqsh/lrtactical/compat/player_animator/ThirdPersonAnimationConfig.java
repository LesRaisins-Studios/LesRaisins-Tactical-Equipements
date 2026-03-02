package me.xjqsh.lrtactical.compat.player_animator;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirdPersonAnimationConfig {
    @SerializedName("animation")
    private String animationPath;

    @SerializedName("upper")
    private Map<String, Object> upper = new HashMap<>();

    @SerializedName("lower")
    private Map<String, Object> lower = new HashMap<>();

    @Nullable
    public ResourceLocation getAnimationPath() {
        return animationPath == null ? null : new ResourceLocation(animationPath);
    }

    @Nullable
    public String getAnimation(PlayerAnimatorIntegration.AnimationLayer layer, String action, int index) {
        Map<String, Object> actions = layer == PlayerAnimatorIntegration.AnimationLayer.UPPER ? upper : lower;
        Object value = actions.get(action);
        if (value == null) return null;

        if (value instanceof String) return (String) value;
        if (value instanceof List<?> list && !list.isEmpty()) {
            Object item = list.get(index % list.size());
            return item instanceof String ? (String) item : null;
        }
        return null;
    }

    public boolean hasMultiple(PlayerAnimatorIntegration.AnimationLayer layer, String action) {
        Map<String, Object> actions = layer == PlayerAnimatorIntegration.AnimationLayer.UPPER ? upper : lower;
        Object value = actions.get(action);
        return value instanceof List<?> list && list.size() > 1;
    }

    public int getCount(PlayerAnimatorIntegration.AnimationLayer layer, String action) {
        Map<String, Object> actions = layer == PlayerAnimatorIntegration.AnimationLayer.UPPER ? upper : lower;
        Object value = actions.get(action);
        if (value instanceof List<?> list) return list.size();
        return value != null ? 1 : 0;
    }

    @Deprecated
    public boolean hasMultipleAnimations(String actionName) {
        return hasMultiple(PlayerAnimatorIntegration.AnimationLayer.UPPER, actionName);
    }

    @Deprecated
    public int getAnimationCount(String actionName) {
        return getCount(PlayerAnimatorIntegration.AnimationLayer.UPPER, actionName);
    }
}
