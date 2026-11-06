package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.network.DataType;
import me.xjqsh.lrtactical.resource.CommonNetworkCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record SPackSyncMessage(Map<DataType, Map<ResourceLocation, String>> cache) implements CustomPacketPayload {
    public static final Type<SPackSyncMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "s_pack_sync_message"));
    public static final StreamCodec<FriendlyByteBuf, SPackSyncMessage> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), SPackSyncMessage::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(SPackSyncMessage message, FriendlyByteBuf buf) {
        buf.writeMap(message.cache(), FriendlyByteBuf::writeEnum, (buf1, map) -> {
            buf1.writeMap(map, (b, r) -> b.writeResourceLocation(r), (b, text) -> b.writeUtf(text));
        });
    }

    public static SPackSyncMessage decode(FriendlyByteBuf buf) {
        var map = buf.readMap(buf1 -> buf1.readEnum(DataType.class), buf2 -> {
            return buf2.readMap(b -> b.readResourceLocation(), b -> b.readUtf());
        });
        return new SPackSyncMessage(map);
    }

    public static void handle(SPackSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> doSync(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void doSync(SPackSyncMessage message) {
        CommonNetworkCache.INSTANCE.fromNetwork(message.cache);
    }
}
