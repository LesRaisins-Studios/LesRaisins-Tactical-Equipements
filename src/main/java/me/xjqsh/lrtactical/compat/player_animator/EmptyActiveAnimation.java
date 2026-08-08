package me.xjqsh.lrtactical.compat.player_animator;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.jetbrains.annotations.NotNull;

/**
 * Stable identity animation used only to keep a modifier layer active.
 */
public class EmptyActiveAnimation implements IAnimation {
    @Override
    public void tick() {
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        return value0;
    }

    @Override
    public void setupAnim(float tickDelta) {
    }

    @Override
    public @NotNull FirstPersonMode getFirstPersonMode(float tickDelta) {
        return FirstPersonMode.DISABLED;
    }
}
