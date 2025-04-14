package me.xjqsh.lrtactical.api.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface ITargetFilter {
    @NotNull List<Entity> filterTargets(LivingEntity source);
}
