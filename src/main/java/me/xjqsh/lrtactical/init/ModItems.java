package me.xjqsh.lrtactical.init;


import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.item.FlashShieldItem;
import me.xjqsh.lrtactical.item.MeleeItem;
import me.xjqsh.lrtactical.item.ThrowableItem;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EquipmentMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> THROWABLE_TAB = TABS.register("throwable",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.throwable"))
                    .icon(ModItems::getThrowableIcon)
                    .displayItems(ModItems::fillThrowables)
                    .build()
    );
    public static final RegistryObject<CreativeModeTab> MELEE_TAB = TABS.register("melee",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.melee"))
                    .icon(ModItems::getMeleeIcon)
                    .displayItems(ModItems::fillMeleeWeapons)
                    .withTabsBefore(THROWABLE_TAB.getId())
                    .build()
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EquipmentMod.MOD_ID);
    public static RegistryObject<ThrowableItem> THROWABLE = ITEMS.register("throwable", ThrowableItem::new);
    public static RegistryObject<MeleeItem> MELEE = ITEMS.register("melee", MeleeItem::new);
    public static RegistryObject<FlashShieldItem> FLASH_SHIELD = ITEMS.register("flash_shield", FlashShieldItem::new);

    public static ItemStack getThrowableIcon() {
        ItemStack stack = new ItemStack(THROWABLE.get());
        IThrowable iThrowable = IThrowable.of(stack);
        if (iThrowable != null) {
            iThrowable.setId(stack, new ResourceLocation(EquipmentMod.MOD_ID, "m67"));
        }
        return stack;
    }

    public static ItemStack getMeleeIcon() {
        ItemStack stack = new ItemStack(MELEE.get());
        IMeleeWeapon iMeleeWeapon = IMeleeWeapon.of(stack);
        if (iMeleeWeapon != null) {
            iMeleeWeapon.setId(stack, new ResourceLocation(EquipmentMod.MOD_ID, "karambit"));
        }
        return stack;
    }


    public static void fillThrowables(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        for (ThrowableIndex<?, ?> index : LrTacticalAPI.getThrowableIndexes()) {
            ItemStack stack = index.createItemStack();
            pOutput.accept(stack);
        }
    }

    public static void fillMeleeWeapons(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        for (MeleeWeaponIndex<?> index : LrTacticalAPI.getMeleeIndexes()) {
            ItemStack stack = index.createItemStack();
            pOutput.accept(stack);
        }
        pOutput.accept(new ItemStack(FLASH_SHIELD.get()));
    }
}