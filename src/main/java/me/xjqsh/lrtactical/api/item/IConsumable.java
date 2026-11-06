package me.xjqsh.lrtactical.api.item;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public interface IConsumable extends ICustomItem {
    String ID_TAG = "ConsumableId";
    String OVERRIDE_DISPLAY_ID = "DisplayId";
    ResourceLocation EMPTY = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "empty");

    static IConsumable of(ItemStack stack) {
        if (stack.getItem() instanceof IConsumable item) {
            return item;
        }
        return null;
    }

    @Override
    default ResourceLocation getId(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt.contains(ID_TAG, Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(nbt.getString(ID_TAG));
            return Objects.requireNonNullElse(rl, EMPTY);
        }
        return EMPTY;
    }

    @Override
    default ResourceLocation getDisplayId(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt.contains(OVERRIDE_DISPLAY_ID, Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(nbt.getString(OVERRIDE_DISPLAY_ID));
            return Objects.requireNonNullElse(rl, EMPTY);
        }
        return getId(stack);
    }

    @Override
    default void setId(ItemStack stack, ResourceLocation id) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            CompoundTag tag = data.copyTag();
            tag.putString(ID_TAG, id.toString());
            return CustomData.of(tag);
        });
    }

    @Override
    default boolean isSame(ItemStack i, ItemStack j) {
        IConsumable c1 = IConsumable.of(i);
        IConsumable c2 = IConsumable.of(j);
        if (c1 != null && c2 != null) {
            return c1.getId(i).equals(c2.getId(j));
        }
        if (i.isEmpty() || j.isEmpty()) {
            return i.isEmpty() && j.isEmpty();
        }
        return false;
    }

    @Override
    default Optional<ResourceLocation> getCoolDownId(ItemStack stack) {
        return getConsumableIndex(stack).map(index -> index.getData().getCooldownCategory());
    }

    @Override
    default int getMaxUsingTick(ItemStack stack) {
        return getConsumableIndex(stack).map(index -> index.getData().getUseDuration()).orElse(0);
    }

    default Optional<ConsumableIndex> getConsumableIndex(ItemStack stack) {
        return LrTacticalAPI.getConsumableIndex(stack);
    }

    default boolean spawnParticleOnBreak(ItemStack stack) {
        return false;
    }
}
