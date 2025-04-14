package me.xjqsh.lrtactical.api.item;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public interface IThrowable extends ICustomItem {
    String ID_TAG = "ThrowableId";
    ResourceLocation EMPTY = new ResourceLocation(EquipmentMod.MOD_ID, "empty");

    static IThrowable of(ItemStack stack) {
        if (stack.getItem() instanceof IThrowable item) {
            return item;
        }
        return null;
    }

    @Override
    default ResourceLocation getId(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(ID_TAG, Tag.TAG_STRING)) {
            ResourceLocation gunId = ResourceLocation.tryParse(nbt.getString(ID_TAG));
            return Objects.requireNonNullElse(gunId, EMPTY);
        }
        return EMPTY;
    }

    @Override
    default void setId(ItemStack stack, ResourceLocation id) {
        stack.getOrCreateTag().putString(ID_TAG, id.toString());
    }

    @Override
    default boolean isSame(ItemStack i, ItemStack j) {
        IThrowable iThrowable1 = IThrowable.of(i);
        IThrowable iThrowable2 = IThrowable.of(j);
        if (iThrowable1 != null && iThrowable2 != null) {
            return iThrowable1.getId(i).equals(iThrowable2.getId(j));
        }
        if (i.isEmpty() || j.isEmpty()) {
            return i.isEmpty() && j.isEmpty();
        }
        return false;
    }

    @Override
    default Optional<ResourceLocation> getCoolDownId(ItemStack stack) {
        return getThrowableIndex(stack)
                .map(index -> index.getData().getCooldownCategory());
    }

    default int getMaxUsingTick(ItemStack stack) {
        return getThrowableIndex(stack)
                .map(index -> index.getData().getPrepareTime())
                .orElse(0);
    }

    default Optional<ThrowableIndex<?, ?>> getThrowableIndex(ItemStack stack) {
        return LrTacticalAPI.getThrowableIndex(stack);
    }
}
