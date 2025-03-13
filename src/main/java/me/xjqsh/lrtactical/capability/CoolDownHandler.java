package me.xjqsh.lrtactical.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CoolDownHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Player player = event.player;
            player.getCapability(CoolDownCapabilityProvider.CAPABILITY).ifPresent(CustomItemCoolDowns::tick);
        }
    }
}
