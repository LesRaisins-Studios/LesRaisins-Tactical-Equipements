package me.xjqsh.lrtactical.item.index;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.index.ICustomItemIndex;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponData;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponType;
import me.xjqsh.lrtactical.util.DefaultAttrUUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MeleeWeaponIndex<T extends MeleeWeaponData> implements ICustomItemIndex {
    private final MeleeWeaponType<T> type;
    private final Item baseItem;
    private final T data;
    private final ResourceLocation id;
    private final String name;
    private final String tooltip;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private List<FormattedCharSequence> desc;


    private MeleeWeaponIndex(MeleeWeaponType<T> type, T data, String name, String tooltip,
                             ResourceLocation id, Item baseItem) {
        this.type = type;
        this.baseItem = baseItem;
        this.data = data;
        this.id = id;
        this.name = name;
        this.tooltip = tooltip;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        for (var entry : data.getRawAttributes().getAttributes()) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(entry.id());
            if (attribute == null) {
                EquipmentMod.LOGGER.error("Unknown attribute {} for melee weapon {}", entry.id(), id);
                continue;
            }
            builder.put(attribute, new AttributeModifier(
                    DefaultAttrUUIDUtil.getUUID(entry.id()),
                    "LesRaisins Custom Item",
                    entry.amount(),
                    entry.operation()
            ));
        }
        defaultModifiers = builder.build();
    }

    @Nullable
    public static <T extends MeleeWeaponData> MeleeWeaponIndex<T> deserialize(
            @NotNull MeleeWeaponType<T> type, JsonElement data, String name, String tooltip, ResourceLocation id, Item baseItem
    ) {
        T meleeData = type.serializer().parse(data);
        if (meleeData == null) {
            return null;
        }
        return new MeleeWeaponIndex<>(type, meleeData, name, tooltip, id, baseItem);
    }

    public Multimap<Attribute, AttributeModifier> getDefaultModifiers() {
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
        return stack;
    }

    @Override
    public ResourceLocation getId() {
        return id;
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
