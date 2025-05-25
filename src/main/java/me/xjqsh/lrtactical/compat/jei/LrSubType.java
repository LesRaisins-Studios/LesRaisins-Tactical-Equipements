package me.xjqsh.lrtactical.compat.jei;

import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import net.minecraft.world.item.ItemStack;

public class LrSubType {
    public static IIngredientSubtypeInterpreter<ItemStack> getMeleeSubtype() {
        return (stack, context) -> {
            if (stack.getItem() instanceof IMeleeWeapon iMeleeWeapon) {
                return iMeleeWeapon.getId(stack).toString();
            }
            return IIngredientSubtypeInterpreter.NONE;
        };
    }

    public static IIngredientSubtypeInterpreter<ItemStack> getThrowableSubtype() {
        return (stack, context) -> {
            if (stack.getItem() instanceof IThrowable iThrowable) {
                return iThrowable.getId(stack).toString();
            }
            return IIngredientSubtypeInterpreter.NONE;
        };
    }
}
