package me.xjqsh.lrtactical.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class BackstabEnchantment extends Enchantment {
    public static final EquipmentSlot[] SUITABLE_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.MAINHAND
    };

    public BackstabEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, SUITABLE_SLOTS);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}

