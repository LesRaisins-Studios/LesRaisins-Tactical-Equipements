package me.xjqsh.lrtactical.api.event;

import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ConsumableUseEvent extends Event {
    private final LivingEntity user;
    private final ItemStack stack;
    private final ResourceLocation consumableId;
    private final ConsumableIndex index;

    public ConsumableUseEvent(LivingEntity user, ItemStack stack, ResourceLocation consumableId, ConsumableIndex index) {
        this.user = user;
        this.stack = stack;
        this.consumableId = consumableId;
        this.index = index;
    }

    public LivingEntity getUser() {
        return user;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ResourceLocation getConsumableId() {
        return consumableId;
    }

    public ConsumableIndex getIndex() {
        return index;
    }
}
