package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, EquipmentMod.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> GRENADE_BOUNCE = SOUNDS.register("grenade_bounce",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "entity.grenade.bounce"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DEAFENED_RING = SOUNDS.register("player_ring",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "player.ring"))
    );

}
