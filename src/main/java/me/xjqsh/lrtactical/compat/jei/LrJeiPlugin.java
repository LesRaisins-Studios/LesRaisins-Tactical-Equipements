package me.xjqsh.lrtactical.compat.jei;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class LrJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(EquipmentMod.MOD_ID, "jei");

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.MELEE.get(), LrSubType.getMeleeSubtype());
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.THROWABLE.get(), LrSubType.getThrowableSubtype());
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
}
