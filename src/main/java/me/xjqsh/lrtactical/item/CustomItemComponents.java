package me.xjqsh.lrtactical.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class CustomItemComponents {
    private CustomItemComponents() {
    }

    public static void apply(ItemStack stack, int maxStackSize, int maxDamage) {
        if (maxDamage > 0) {
            stack.set(DataComponents.MAX_STACK_SIZE, 1);
            stack.set(DataComponents.MAX_DAMAGE, maxDamage);
            if (!stack.has(DataComponents.DAMAGE)) {
                stack.set(DataComponents.DAMAGE, 0);
            }
        } else {
            stack.remove(DataComponents.MAX_DAMAGE);
            stack.remove(DataComponents.DAMAGE);
            stack.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
        }
    }
}
