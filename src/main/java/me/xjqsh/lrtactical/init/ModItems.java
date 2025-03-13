package me.xjqsh.lrtactical.init;


import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.item.ThrowableItem;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
                    .icon(() -> new ItemStack(ModItems.THROWABLE.get()))
                    .displayItems(ModItems::fillThrowables)
                    .build()
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EquipmentMod.MOD_ID);
    public static RegistryObject<ThrowableItem> THROWABLE = ITEMS.register("throwable", ThrowableItem::new);


    public static void fillThrowables(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        for (ThrowableIndex<?, ?> index : LrTacticalAPI.getThrowableIndexes()) {
            ItemStack stack = index.createItemStack();
            pOutput.accept(stack);
        }
    }
}