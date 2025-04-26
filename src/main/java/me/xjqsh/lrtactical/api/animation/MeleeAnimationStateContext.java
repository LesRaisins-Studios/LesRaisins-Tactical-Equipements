package me.xjqsh.lrtactical.api.animation;

import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import me.xjqsh.lrtactical.api.item.IThrowable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class MeleeAnimationStateContext extends ItemAnimationStateContext {
    private ItemStack currentItem = ItemStack.EMPTY;
    private int usingTick = 0;
    private boolean using = false;
    private int prepareTime = 0;
    private float walkDistAnchor = 0f;

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
        if (currentItem.getItem() instanceof IThrowable iThrowable) {
            this.prepareTime = iThrowable.getThrowableIndex(currentItem)
                    .map(index -> index.getData().getPrepareTime())
                    .orElse(0);
        }
    }

    private <T> Optional<T> processCameraEntity(Function<Entity, T> processor) {
        Entity entity = Minecraft.getInstance().cameraEntity;
        if (entity != null) {
            return Optional.ofNullable(processor.apply(entity));
        }
        return Optional.empty();
    }

    /**
     * 获取当前系统时间，单位毫秒。
     * @return 当前系统时间
     */
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取玩家的按键输入是否为上。
     * @return 玩家的按键输入是否为上 (对应着移动中的前进按键，如 W)
     */
    public boolean isInputUp() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.up).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为下。
     * @return 玩家的按键输入是否为下 (对应着移动中的后退按键，如 S)
     */
    public boolean isInputDown() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.down).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为左。
     * @return 玩家的按键输入是否为左 (对应着移动中的左移按键，如 A)
     */
    public boolean isInputLeft() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.left).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为右。
     * @return 玩家的按键输入是否为右 (对应着移动中的右移按键，如 D)
     */
    public boolean isInputRight() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.right).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为跳跃。
     * @return 玩家的按键输入是否为跳跃 (对应着移动中的跳跃按键，如 Space)
     */
    public boolean isInputJumping() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.jumping).orElse(false);
    }

    /**
     * 获取玩家是否接触地面
     * @return 玩家是否接触地面
     */
    public boolean isOnGround() {
        return processCameraEntity(Entity::onGround).orElse(false);
    }

    /**
     * 获取 玩家是否蹲伏
     * @return 玩家是否蹲伏
     */
    public boolean isCrouching() {
        return processCameraEntity(Entity::isCrouching).orElse(false);
    }

    /**
     * 在玩家当前的行走距离打上锚点。此后，getWalkDist() 将返回与此锚点的相对值
     */
    public void anchorWalkDist() {
        processCameraEntity(entity -> {
            walkDistAnchor = entity.walkDist + (entity.walkDist - entity.walkDistO) * partialTicks;
            return null;
        });
    }

    /**
     * 获取与锚点相对的行走距离。如果没有打锚点，则直接获取行走距离。
     * @return 与锚点相对的行走距离。如果没有打锚点，则直接返回行走距离。
     */
    public float getWalkDist() {
        return processCameraEntity(entity -> {
            float currentWalkDist = entity.walkDist + (entity.walkDist - entity.walkDistO) * partialTicks;
            return currentWalkDist - walkDistAnchor;
        }).orElse(0f);
    }
}
