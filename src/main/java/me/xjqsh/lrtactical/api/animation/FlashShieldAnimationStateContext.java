package me.xjqsh.lrtactical.api.animation;

import me.xjqsh.lrtactical.item.FlashShieldItem;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class FlashShieldAnimationStateContext extends BaseAnimationStateContext {
    private ItemStack currentItem = ItemStack.EMPTY;
    private int usingTick = 0;
    private boolean using = false;
    private int prepareTime = 0;
    private float walkDistAnchor = 0f;

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
        if (currentItem.getItem() instanceof FlashShieldItem flashShieldItem) {
            this.prepareTime = flashShieldItem.getMaxUsingTick(currentItem);
        }
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
