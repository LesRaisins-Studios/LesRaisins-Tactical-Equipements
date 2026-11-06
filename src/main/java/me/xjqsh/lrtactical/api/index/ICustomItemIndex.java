package me.xjqsh.lrtactical.api.index;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ICustomItemIndex {
    ItemStack createItemStack();

    int getMaxStackSize();

    ResourceLocation getId();

    /** 同一命名空间内的排列顺序，数值越小越靠前，范围为 0–65536。 */
    default int getSort() {
        return 0;
    }

    Item getBaseItem();

    String getDescriptionId();
}
