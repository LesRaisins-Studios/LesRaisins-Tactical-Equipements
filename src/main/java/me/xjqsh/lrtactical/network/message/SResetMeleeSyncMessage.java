package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.capability.CombatProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

public record SResetMeleeSyncMessage() implements CustomPacketPayload {
    public static final Type<SResetMeleeSyncMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "s_reset_melee_sync_message"));
    public static final StreamCodec<FriendlyByteBuf, SResetMeleeSyncMessage> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), SResetMeleeSyncMessage::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(SResetMeleeSyncMessage message, FriendlyByteBuf buf) {
    }

    public static SResetMeleeSyncMessage decode(FriendlyByteBuf buf) {
        return new SResetMeleeSyncMessage();
    }

    public static void handle(SResetMeleeSyncMessage message, IPayloadContext context) {
        context.enqueueWork(SResetMeleeSyncMessage::handleClient);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        CombatProperties.get(player).ifPresent(CombatProperties::resetMeleeSync);
    }
}
