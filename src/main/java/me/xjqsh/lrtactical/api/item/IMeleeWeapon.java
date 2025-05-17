package me.xjqsh.lrtactical.api.item;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

public interface IMeleeWeapon extends ICustomItem {
    String ID_TAG = "MeleeWeaponId";
    String OVERRIDE_DISPLAY_ID = "DisplayId";
    ResourceLocation EMPTY = new ResourceLocation(EquipmentMod.MOD_ID, "empty");

    static IMeleeWeapon of(ItemStack stack) {
        if (stack.getItem() instanceof IMeleeWeapon item) {
            return item;
        }
        return null;
    }

    @Override
    default ResourceLocation getId(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(ID_TAG, Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(nbt.getString(ID_TAG));
            return Objects.requireNonNullElse(rl, EMPTY);
        }
        return EMPTY;
    }

    @Override
    default ResourceLocation getDisplayId(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(OVERRIDE_DISPLAY_ID, Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(nbt.getString(OVERRIDE_DISPLAY_ID));
            return Objects.requireNonNullElse(rl, EMPTY);
        }
        return getId(stack);
    }

    @Override
    default void setId(ItemStack stack, ResourceLocation id) {
        stack.getOrCreateTag().putString(ID_TAG, id.toString());
    }

    @Override
    default boolean shouldBlockAttack() {
        return true;
    }

    @Override
    default boolean shouldBlockUse() {
        return true;
    }

    default int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action) {
        return 0;
    }

    default void attack(Player attacker, ItemStack stack, MeleeAction action) {
        this.attack(attacker, stack, action, attacker.position(), attacker.getLookAngle());
    }

    default Optional<MeleeWeaponIndex<?>> getMeleeIndex(ItemStack stack) {
        return LrTacticalAPI.getMeleeIndex(stack);
    }

    void attack(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction);
}
