package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@EventBusSubscriber(modid = EquipmentMod.MOD_ID)
public final class ItemAttributesHandler {
    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        LrTacticalAPI.getMeleeIndex(event.getItemStack())
                .ifPresent(
                        index -> {
                            for (var entry : index.getDefaultModifiers().modifiers()) {
                                event.replaceModifier(
                                        entry.attribute(), entry.modifier(), entry.slot());
                            }
                        });
    }
}
