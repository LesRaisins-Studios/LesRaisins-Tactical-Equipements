package me.xjqsh.lrtactical.item.throwable;

import com.google.gson.JsonElement;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record ThrowableType<T extends ThrowableData, E extends ThrowableItemEntity>(
        ThrowableType.ThrowableFactory<T, E> factory,
        ThrowableType.ThrowableDataSerializer<T> serializer
) {

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
