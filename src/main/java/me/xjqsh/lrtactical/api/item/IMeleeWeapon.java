package me.xjqsh.lrtactical.api.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IMeleeWeapon extends ICustomItem {
    @Override
    default ResourceLocation getId(ItemStack stack) {
        return new ResourceLocation("lrtactical", "melee_weapon");
    }

    @Override
    default void setId(ItemStack stack, ResourceLocation id) {
    }

    @Override
    default boolean shouldBlockAttack() {
        return true;
    }

    @Override
    default boolean shouldBlockUse() {
        return true;
    }

    void attack(Player attacker, ItemStack stack);
}
