package me.xjqsh.lrtactical.api.animation;

import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import net.minecraft.world.item.ItemStack;

public class ConsumableAnimationStateContext extends ItemAnimationStateContext {
    private ItemStack currentItem = ItemStack.EMPTY;
    private boolean using = false;
    private int usingTick = 0;

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }

    public int getStackCount() {
        return currentItem.getCount();
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }

    public int getUsingTick() {
        return usingTick;
    }

    public void setUsingTick(int usingTick) {
        this.usingTick = usingTick;
    }

}
