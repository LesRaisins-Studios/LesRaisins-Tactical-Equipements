package me.xjqsh.lrtactical.item.index;

import com.google.gson.JsonElement;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.index.ICustomItemIndex;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponData;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponType;
import me.xjqsh.lrtactical.util.DefaultAttrUUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeleeWeaponIndex<T extends MeleeWeaponData> implements ICustomItemIndex {
    private final MeleeWeaponType<T> type;
    private final Item baseItem;
    private final T data;
    private final ResourceLocation id;
    private final String name;
    private final String tooltip;
    private final int sort;
    private final ItemAttributeModifiers defaultModifiers;

    private MeleeWeaponIndex(MeleeWeaponType<T> type, T data, String name, String tooltip,
                             ResourceLocation id, Item baseItem, int sort) {
        this.type = type;
        this.baseItem = baseItem;
        this.data = data;
        this.id = id;
        this.name = name;
        this.tooltip = tooltip;
        this.sort = Mth.clamp(sort, 0, 65536);
        var builder = ItemAttributeModifiers.builder();
        for (var entry : data.getRawAttributes().getAttributes()) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(entry.id());
            if (attribute == null) {
                EquipmentMod.LOGGER.error("Unknown attribute {} for melee weapon {}", entry.id(), id);
                continue;
            }
            builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), new AttributeModifier(
                    DefaultAttrUUIDUtil.getId(entry.id()),
                    entry.amount(),
                    entry.operation()
            ), EquipmentSlotGroup.MAINHAND);
        }
        defaultModifiers = builder.build();
    }

    @Nullable
    public static <T extends MeleeWeaponData> MeleeWeaponIndex<T> deserialize(
            @NotNull MeleeWeaponType<T> type, JsonElement data, String name, String tooltip, ResourceLocation id, Item baseItem
    ) {
        return deserialize(type, data, name, tooltip, id, baseItem, 0);
    }

    @Nullable
    public static <T extends MeleeWeaponData> MeleeWeaponIndex<T> deserialize(
            @NotNull MeleeWeaponType<T> type, JsonElement data, String name, String tooltip, ResourceLocation id, Item baseItem, int sort
    ) {
        T meleeData = type.serializer().parse(data);
        if (meleeData == null) {
            return null;
        }
        return new MeleeWeaponIndex<>(type, meleeData, name, tooltip, id, baseItem, sort);
    }

    public ItemAttributeModifiers getDefaultModifiers() {
        return defaultModifiers;
    }

    public T getData() {
        return data;
    }

    public MeleeWeaponType<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getTooltip() {
        return tooltip;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(baseItem);
        if (stack.getItem() instanceof IMeleeWeapon iMeleeWeapon) {
            iMeleeWeapon.setId(stack, this.getId());
        }
        if (data.getMaxDurability() > 0) {
            stack.set(DataComponents.MAX_DAMAGE, data.getMaxDurability());
            stack.set(DataComponents.DAMAGE, 0);
        }
        return stack;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public int getMaxDurability() {
        return data.getMaxDurability();
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
}
