package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EquipmentMod.MOD_ID);
    public static final RegistryObject<SoundEvent> GRENADE_BOUNCE = SOUNDS.register("grenade_bounce",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(EquipmentMod.MOD_ID, "entity.grenade.bounce"))
    );
    public static final RegistryObject<SoundEvent> DEAFENED_RING = SOUNDS.register("player_ring",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(EquipmentMod.MOD_ID, "player.ring"))
    );

}
