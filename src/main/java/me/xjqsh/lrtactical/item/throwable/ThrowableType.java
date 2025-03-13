package me.xjqsh.lrtactical.item.throwable;

import com.google.gson.JsonElement;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ThrowableType<T extends ThrowableData, E extends ThrowableItemEntity> {
    private final ThrowableFactory<T, E> factory;
    private final ThrowableDataSerializer<T> serializer;
    private Item item;

    public ThrowableType(ThrowableFactory<T, E> factory, ThrowableDataSerializer<T> serializer) {
        this.factory = factory;
        this.serializer = serializer;
    }

    public ThrowableFactory<T, E> getFactory() {
        return factory;
    }

    public ThrowableDataSerializer<T> getSerializer() {
        return serializer;
    }


    @FunctionalInterface
    public interface ThrowableFactory<T extends ThrowableData, E extends ThrowableItemEntity> {
        E create(ItemStack stack, LivingEntity thrower, T data);
    }

    @FunctionalInterface
    public interface ThrowableDataSerializer<T extends ThrowableData> {
        T parse(JsonElement json);
    }

    public static class Builder<T extends ThrowableData, E extends ThrowableItemEntity> {
        private ThrowableFactory<T, E> factory;
        private ThrowableDataSerializer<T> serializer;

        public static <T extends ThrowableData, E extends ThrowableItemEntity> Builder<T, E> of() {
            return new Builder<>();
        }

        public Builder<T, E> setFactory(ThrowableFactory<T, E> factory) {
            this.factory = factory;
            return this;
        }

        public Builder<T, E> setSerializer(ThrowableDataSerializer<T> serializer) {
            this.serializer = serializer;
            return this;
        }

        public ThrowableType<T, E> build() {
            return new ThrowableType<>(factory, serializer);
        }
    }
}
