package me.xjqsh.lrtactical.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombatPropertiesProvider implements ICapabilityProvider {
    public static final Capability<CombatProperties> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final CombatProperties instance;

    public CombatPropertiesProvider(Player entity) {
        this.instance = new CombatProperties(entity);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, LazyOptional.of(() -> instance).cast());
    }
}
