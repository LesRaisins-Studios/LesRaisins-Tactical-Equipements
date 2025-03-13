package me.xjqsh.lrtactical.item.index;

import com.google.gson.*;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import me.xjqsh.lrtactical.init.ModItems;
import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThrowableIndex<T extends ThrowableData, E extends ThrowableItemEntity> {
    private final ThrowableType<T, E> type;
    private final Item baseItem;
    private final T data;
    private final ResourceLocation id;
    private final String name;

    private ThrowableIndex(@NotNull ThrowableType<T, E> type, T data,
                          String name, ResourceLocation id, Item baseItem) {
        this.type = type;
        this.data = data;
        this.id = id;
        this.baseItem = baseItem;
        this.name = name;
    }

    @Nullable
    public static <T extends ThrowableData, E extends ThrowableItemEntity> ThrowableIndex<T, E> deserialize(
            @NotNull ThrowableType<T, E> type, JsonElement data, String name, ResourceLocation id, Item baseItem
    ) {
        T throwableData = type.getSerializer().parse(data);
        if (throwableData == null) {
            return null;
        }
        return new ThrowableIndex<>(type, throwableData, name, id, baseItem);
    }

    public ResourceLocation getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public ThrowableType<T, E> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public E createEntity(ItemStack stack, LivingEntity thrower) {
        return type.getFactory().create(stack, thrower, data);
    }

    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(baseItem);
        if (stack.getItem() instanceof IThrowable iThrowable) {
            iThrowable.setId(stack, this.getId());
        }
        return stack;
    }
}
