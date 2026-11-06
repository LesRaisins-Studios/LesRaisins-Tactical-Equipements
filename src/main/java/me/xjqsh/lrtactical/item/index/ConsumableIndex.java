package me.xjqsh.lrtactical.item.index;

import com.google.gson.JsonElement;
import me.xjqsh.lrtactical.api.index.ICustomItemIndex;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.item.consumable.ConsumableData;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Nullable;

public class ConsumableIndex implements ICustomItemIndex {
    private final Item baseItem;
    private final ConsumableData data;
    private final ResourceLocation id;
    private final String name;
    private final String tooltip;
    private final int sort;
    private final FoodProperties foodProperties;

    private ConsumableIndex(ConsumableData data, String name, String tooltip, ResourceLocation id, Item baseItem, int sort) {
        this.baseItem = baseItem;
        this.data = data;
        this.id = id;
        this.name = name;
        this.tooltip = tooltip;
        this.sort = Mth.clamp(sort, 0, 65536);
        this.foodProperties = buildFoodProperties(data);
    }

    private static FoodProperties buildFoodProperties(ConsumableData data) {
        if (data.getFood() <= 0 && data.getSaturation() <= 0f && data.getEffects().isEmpty()) {
            return null;
        }
        FoodProperties.Builder builder = new FoodProperties.Builder()
                .nutrition(data.getFood())
                .saturationModifier(data.getSaturation());
        for (ConsumableData.EffectData effectData : data.getEffects()) {
            builder.effect(effectData::createInstance, effectData.getChance());
        }
        return builder.build();
    }

    @Nullable
    public static ConsumableIndex deserialize(JsonElement data, String name, String tooltip, ResourceLocation id, Item baseItem) {
        return deserialize(data, name, tooltip, id, baseItem, 0);
    }

    @Nullable
    public static ConsumableIndex deserialize(JsonElement data, String name, String tooltip, ResourceLocation id, Item baseItem, int sort) {
        ConsumableData consumableData = CommonAssetsManager.GSON.fromJson(data, ConsumableData.class);
        if (consumableData == null) {
            return null;
        }
        return new ConsumableIndex(consumableData, name, tooltip, id, baseItem, sort);
    }

    public ConsumableData getData() {
        return data;
    }

    /**
     * 获取 tooltip 描述翻译键，可为 null
     */
    @Nullable
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(baseItem);
        if (stack.getItem() instanceof IConsumable consumable) {
            consumable.setId(stack, this.getId());
        }
        stack.set(DataComponents.MAX_STACK_SIZE, getMaxStackSize());
        if (data.hasDurability()) {
            stack.set(DataComponents.MAX_DAMAGE, data.getMaxDurability());
            stack.set(DataComponents.DAMAGE, 0);
        }
        return stack;
    }

    @Override
    public int getMaxStackSize() {
        if (data.hasDurability()) {
            return 1;
        }
        return data.getStackSize();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public int getSort() {
        return sort;
    }

    @Override
    public Item getBaseItem() {
        return baseItem;
    }

    @Override
    public String getDescriptionId() {
        return name;
    }

    public FoodProperties getFoodProperties() {
        return foodProperties;
    }
}
