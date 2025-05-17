package me.xjqsh.lrtactical.api.item;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * 自定义物品接口，用于使用同一物品id由nbt驱动的具有不同功能的物品
 */
public interface ICustomItem {
    /**
     * 获取物品的自定义id
     *
     * @param stack 物品
     * @return 物品id
     */
    ResourceLocation getId(ItemStack stack);

    /**
     * 获取物品的自定义效果id
     * 默认情况下与id相同
     *
     * @param stack 物品
     * @return 物品id
     */
    default ResourceLocation getDisplayId(ItemStack stack) {
        return getId(stack);
    }

    /**
     * 设置物品的自定义id
     *
     * @param stack 物品
     * @param id    物品id
     */
    void setId(ItemStack stack, ResourceLocation id);

    boolean isSame(ItemStack stack1, ItemStack stack2);

    /**
     * 获取物品的冷却id，用于自定义cd<br/>
     * 冷却的具体限制需要自行在Item中实现<br/>
     * 参见{@link CustomItemCoolDowns}
     *
     * @param stack 物品
     * @return 物品冷却id
     */
    default Optional<ResourceLocation> getCoolDownId(ItemStack stack) {
        return Optional.empty();
    }

    /**
     * 获取物品的最大使用时间<br/>
     * 使用时间的具体限制需要自行在Item中实现<br/>
     * 用于overlay渲染使用进度
     *
     * @param stack 物品
     * @return 最大使用时间，单位tick
     */
    default int getMaxUsingTick(ItemStack stack) {
        return 0;
    }

    /**
     * 获取物品的切入时间<br/>
     * 切入时物品应无法攻击或使用
     *
     * @param stack 物品
     * @return 最大使用时间，单位tick
     */
    default int getDrawTime(ItemStack stack) {
        return 0;
    }

    default int getPutAwayTime(ItemStack stack) {
        return 0;
    }

    default int getAttackCoolDown(ItemStack stack, MeleeAction action) {
        return 20;
    }

    /**
     * 是否阻止攻击事件<br/>
     * 参见{@link net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered}
     * @return 是否阻止点击事件
     */
    default boolean shouldBlockAttack() {
        return false;
    }

    /**
     * 是否阻止使用事件<br/>
     * 参见{@link net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered}
     * @return 是否阻止点击事件
     */
    default boolean shouldBlockUse() {
        return false;
    }

    /**
     * 是否阻止选取方块事件<br/>
     * 参见{@link net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered}
     * @return 是否阻止点击事件
     */
    default boolean shouldBlockPickBlock() {
        return false;
    }
}
