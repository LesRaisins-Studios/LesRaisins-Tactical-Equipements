package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.index.ICustomItemIndex;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collection;
import java.util.Comparator;

public class ModItems {
    private static final ResourceLocation THROWABLE_ICON_ID = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "m67");
    private static final ResourceLocation CONSUMABLE_ICON_ID = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "blood_pack");
    private static final ResourceLocation MELEE_ICON_ID = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "karambit");

    private static final Comparator<ICustomItemIndex> CREATIVE_ITEM_ORDER = Comparator
            .comparingInt((ICustomItemIndex index) -> EquipmentMod.MOD_ID.equals(index.getId().getNamespace()) ? 0 : 1)
            .thenComparing(index -> index.getId().getNamespace())
            .thenComparingInt(ICustomItemIndex::getSort)
            .thenComparing(index -> index.getId().getPath());

    public static final DeferredRegister<CreativeModeTab> TABS =

            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EquipmentMod.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THROWABLE_TAB = TABS.register("throwable",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.throwable"))
                    .icon(ModItems::getThrowableIcon)
                    .displayItems(ModItems::fillThrowables)
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONSUMABLE_TAB = TABS.register("consumable",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.consumable"))
                    .icon(ModItems::getConsumableIcon)
                    .displayItems(ModItems::fillConsumables)
                    .withTabsBefore(THROWABLE_TAB.getId())
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MELEE_TAB = TABS.register("melee",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.melee"))
                    .icon(ModItems::getMeleeIcon)
                    .displayItems(ModItems::fillMeleeWeapons)
                    .withTabsBefore(CONSUMABLE_TAB.getId())
                    .build()
    );

    public static final DeferredRegister<Item> ITEMS =

            DeferredRegister.create(BuiltInRegistries.ITEM, EquipmentMod.MOD_ID);
    public static DeferredHolder<Item, ConsumableItem> CONSUMABLE = ITEMS.register("consumable", ConsumableItem::new);
    public static DeferredHolder<Item, ThrowableItem> THROWABLE = ITEMS.register("throwable", ThrowableItem::new);
    public static DeferredHolder<Item, MeleeItem> MELEE = ITEMS.register("melee", MeleeItem::new);
    public static DeferredHolder<Item, FlashShieldItem> FLASH_SHIELD = ITEMS.register("flash_shield", FlashShieldItem::new);
    public static DeferredHolder<Item, DetonatorItem> DETONATOR = ITEMS.register("detonator", DetonatorItem::new);

    public static ItemStack getThrowableIcon() {
        ItemStack stack = new ItemStack(THROWABLE.get());
        IThrowable iThrowable = IThrowable.of(stack);
        if (iThrowable != null) {
            iThrowable.setId(stack, THROWABLE_ICON_ID);
        }
        return stack;
    }

    public static ItemStack getConsumableIcon() {
        ItemStack stack = new ItemStack(CONSUMABLE.get());
        IConsumable consumable = IConsumable.of(stack);
        if (consumable != null) {
            consumable.setId(stack, CONSUMABLE_ICON_ID);
        }
        return stack;
    }

    public static ItemStack getMeleeIcon() {
        ItemStack stack = new ItemStack(MELEE.get());
        IMeleeWeapon iMeleeWeapon = IMeleeWeapon.of(stack);
        if (iMeleeWeapon != null) {
            iMeleeWeapon.setId(stack, MELEE_ICON_ID);
        }
        return stack;
    }

    public static void fillConsumables(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        fillItems(LrTacticalAPI.getConsumableIndexes(), pOutput);
    }

    public static void fillThrowables(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        fillItems(LrTacticalAPI.getThrowableIndexes(), pOutput);
        pOutput.accept(new ItemStack(DETONATOR.get()));
    }

    public static void fillMeleeWeapons(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        fillItems(LrTacticalAPI.getMeleeIndexes(), pOutput);
        pOutput.accept(new ItemStack(FLASH_SHIELD.get()));
    }

    private static void fillItems(Collection<? extends ICustomItemIndex> indexes, CreativeModeTab.Output output) {
        indexes.stream().sorted(CREATIVE_ITEM_ORDER).map(ICustomItemIndex::createItemStack).forEach(output::accept);
    }
}
