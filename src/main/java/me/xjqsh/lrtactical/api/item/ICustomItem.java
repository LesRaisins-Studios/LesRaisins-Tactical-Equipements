package me.xjqsh.lrtactical.api.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface ICustomItem {
    ResourceLocation getId(ItemStack stack);

    void setId(ItemStack stack, ResourceLocation id);

    Optional<ResourceLocation> getCoolDownId(ItemStack stack);

    int getMaxUsingTick(ItemStack stack);
}
