package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.client.ClientEventsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SShakeScreenMessage(double time, double radius, double amplitude, Vec3 position) {

    public static void encode(SShakeScreenMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.time);
        buffer.writeDouble(message.radius);
        buffer.writeDouble(message.amplitude);
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
    }

    public static SShakeScreenMessage decode(FriendlyByteBuf buffer) {
        return new SShakeScreenMessage(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
        );
    }

    public static void handle(SShakeScreenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> ClientEventsHandler.handleShakeClient(
                    message.time, message.radius, message.amplitude, message.position
            ));
        }
        context.setPacketHandled(true);
    }
}
