package me.xjqsh.lrtactical.api.item;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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

    int getAttackDelay(Player attacker, ItemStack stack, MeleeAction action);

    default void attack(Player attacker, ItemStack stack, MeleeAction action) {
        this.attack(attacker, stack, action, attacker.position(), attacker.getLookAngle());
    }

    void attack(Player attacker, ItemStack stack, MeleeAction action, Vec3 origin, Vec3 direction);
}
