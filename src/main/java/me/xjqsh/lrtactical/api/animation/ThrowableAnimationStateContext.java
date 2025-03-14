package me.xjqsh.lrtactical.api.animation;

import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import me.xjqsh.lrtactical.api.item.IThrowable;
import net.minecraft.world.item.ItemStack;

public class ThrowableAnimationStateContext extends ItemAnimationStateContext {
    private ItemStack currentItem = ItemStack.EMPTY;
    private int usingTick = 0;
    private boolean using = false;
    private int prepareTime = 0;

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
        if (currentItem.getItem() instanceof IThrowable iThrowable) {
            this.prepareTime = iThrowable.getThrowableIndex(currentItem)
                    .map(index -> index.getData().getPrepareTime())
                    .orElse(0);
        }
    }

    public int getStackCount() {
        return currentItem.getCount();
    }

    public int getUsingTick() {
        return usingTick;
    }

    public void setUsingTick(int throwTime) {
        this.usingTick = throwTime;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }

    public int getPrepareTime() {
        return prepareTime;
    }
}
