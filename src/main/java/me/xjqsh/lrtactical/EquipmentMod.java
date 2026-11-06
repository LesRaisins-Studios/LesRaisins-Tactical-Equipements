package me.xjqsh.lrtactical;

import me.xjqsh.lrtactical.config.ClientConfig;
import me.xjqsh.lrtactical.config.CommonConfig;
import me.xjqsh.lrtactical.config.ServerConfig;
import me.xjqsh.lrtactical.init.*;
import net.neoforged.bus.api.IEventBus;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EquipmentMod.MOD_ID)
public class EquipmentMod {
    public static final String MOD_ID = "lrtactical";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public EquipmentMod(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.init());
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.init());
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.init());

        ModAttachments.TYPES.register(modEventBus);

        ModItems.TABS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModCustomTypes.THROWABLE_TYPES.register(modEventBus);
        ModCustomTypes.MELEE_WEAPON_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

    }

}
