package me.xjqsh.lrtactical.api.index;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ICustomItemIndex {
    ItemStack createItemStack();

    int getMaxStackSize();

    ResourceLocation getId();

    Item getBaseItem();

    String getDescriptionId();
}
