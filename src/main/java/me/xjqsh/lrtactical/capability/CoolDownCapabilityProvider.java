package me.xjqsh.lrtactical.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoolDownCapabilityProvider implements ICapabilityProvider {
    public static final Capability<CustomItemCoolDowns> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final CustomItemCoolDowns instance;

    public CoolDownCapabilityProvider(Player player) {
        this.instance = new CustomItemCoolDowns(player);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, LazyOptional.of(() -> instance).cast());
    }
}
