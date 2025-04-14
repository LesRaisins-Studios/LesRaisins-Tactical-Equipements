package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CMeleeAttack(MeleeAction action) {
    public static void encode(CMeleeAttack message, FriendlyByteBuf buf) {
        buf.writeEnum(message.action);
    }

    public static CMeleeAttack decode(FriendlyByteBuf buf) {
        return new CMeleeAttack(buf.readEnum(MeleeAction.class));
    }

    public static void handle(CMeleeAttack message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                player.getCapability(CombatPropertiesProvider.CAPABILITY).ifPresent(cap -> cap.attack(message.action));
            });
        }
        context.setPacketHandled(true);
    }
}
