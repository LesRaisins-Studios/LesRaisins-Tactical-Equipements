package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

public record SCustomCoolDownMessage(ResourceLocation id, int duration) implements CustomPacketPayload {
    public static final Type<SCustomCoolDownMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "s_custom_cool_down_message"));
    public static final StreamCodec<FriendlyByteBuf, SCustomCoolDownMessage> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), SCustomCoolDownMessage::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(SCustomCoolDownMessage message, FriendlyByteBuf buf) {
        buf.writeResourceLocation(message.id);
        buf.writeInt(message.duration);
    }

    public static SCustomCoolDownMessage decode(FriendlyByteBuf buf) {
        return new SCustomCoolDownMessage(buf.readResourceLocation(), buf.readInt());
    }

    public static void handle(SCustomCoolDownMessage message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SCustomCoolDownMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        CustomItemCoolDowns.get(player).ifPresent(
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
