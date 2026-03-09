package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SResetMeleeSyncMessage() {
    public static void encode(SResetMeleeSyncMessage message, FriendlyByteBuf buf) {
    }

    public static SResetMeleeSyncMessage decode(FriendlyByteBuf buf) {
        return new SResetMeleeSyncMessage();
    }

    public static void handle(SResetMeleeSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(SResetMeleeSyncMessage::handleClient);
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.getCapability(CombatPropertiesProvider.CAPABILITY).ifPresent(CombatProperties::resetMeleeSync);
    }
}
