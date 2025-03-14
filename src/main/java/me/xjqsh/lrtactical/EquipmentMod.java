package me.xjqsh.lrtactical;

import me.xjqsh.lrtactical.init.*;
import me.xjqsh.lrtactical.network.NetworkHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EquipmentMod.MOD_ID)
public class EquipmentMod {
    public static final String MOD_ID = "lrtactical";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public EquipmentMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.TABS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModThrowableTypes.THROWABLE_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        NetworkHandler.init();
    }

}
