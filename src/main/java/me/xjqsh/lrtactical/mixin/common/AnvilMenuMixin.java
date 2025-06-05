package me.xjqsh.lrtactical.mixin.common;

import me.xjqsh.lrtactical.api.item.ICustomItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow @Final private DataSlot cost;

    @Shadow public int repairItemCountCost;

    public AnvilMenuMixin(@Nullable MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(pType, pContainerId, pPlayerInventory, pAccess);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void onCreateResult(CallbackInfo ci) {
        ItemStack stack1 = this.inputSlots.getItem(0);
        ItemStack stack2 = this.inputSlots.getItem(1);
        if (stack1.getItem() instanceof ICustomItem item1 && stack2.getItem() instanceof ICustomItem) {
            if (!item1.isSame(stack1, stack2)) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                this.cost.set(0);
                this.repairItemCountCost = 0;
                ci.cancel();
            }
        }
    }
}
