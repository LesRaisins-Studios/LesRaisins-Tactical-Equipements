package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.client.ClientEventsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

public record SShakeScreenMessage(double time, double radius, double amplitude, Vec3 position) implements CustomPacketPayload {
    public static final Type<SShakeScreenMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "s_shake_screen_message"));
    public static final StreamCodec<FriendlyByteBuf, SShakeScreenMessage> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), SShakeScreenMessage::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

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

    public static void handle(SShakeScreenMessage message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEventsHandler.handleShakeClient(
                message.time, message.radius, message.amplitude, message.position
        ));
    }
}
