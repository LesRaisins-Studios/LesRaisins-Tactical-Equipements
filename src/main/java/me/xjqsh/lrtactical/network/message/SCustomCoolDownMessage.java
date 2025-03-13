package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.capability.CoolDownCapabilityProvider;
import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;
import me.xjqsh.lrtactical.resource.CommonNetworkCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SCustomCoolDownMessage(ResourceLocation id, int duration) {

    public static void encode(SCustomCoolDownMessage message, FriendlyByteBuf buf) {
        buf.writeResourceLocation(message.id);
        buf.writeInt(message.duration);
    }

    public static SCustomCoolDownMessage decode(FriendlyByteBuf buf) {
        return new SCustomCoolDownMessage(buf.readResourceLocation(), buf.readInt());
    }

    public static void handle(SCustomCoolDownMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SCustomCoolDownMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.getCapability(CoolDownCapabilityProvider.CAPABILITY).ifPresent(
            coolDownCapability -> {
                if (message.duration() == 0) {
                    coolDownCapability.removeCooldown(message.id());
                } else {
                    coolDownCapability.addCooldown(message.id(), message.duration());
                }
            }
        );
    }
}