package me.xjqsh.lrtactical.util;

import com.tacz.guns.compat.playeranimator.animation.PlayerAnimatorAssetManager;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

public interface IPAAssetManager {
    @Unique
    Optional<KeyframeAnimation> lrt$getAnimations(ResourceLocation id, String name);

    static Optional<KeyframeAnimation> getAnimations(ResourceLocation id, String name) {
        if (PlayerAnimatorAssetManager.get() instanceof IPAAssetManager ipaAssetManager) {
            return ipaAssetManager.lrt$getAnimations(id, name);
        }
        return Optional.empty();
    }
}
