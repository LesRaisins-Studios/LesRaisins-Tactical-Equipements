package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.event.MeleePreAttackEvent;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;

public class SMeleeAnimationSync implements CustomPacketPayload {
    public static final Type<SMeleeAnimationSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "s_melee_animation_sync"));
    public static final StreamCodec<FriendlyByteBuf, SMeleeAnimationSync> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), SMeleeAnimationSync::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private final int playerId;
    private final MeleeAction state;
    private final int actionCount;
    private final ResourceLocation animationId;

    public SMeleeAnimationSync(int playerId, MeleeAction state, int actionCount, @Nullable ResourceLocation animationId) {
        this.playerId = playerId;
        this.state = state;
        this.actionCount = actionCount;
        this.animationId = animationId;
    }

    public static void encode(SMeleeAnimationSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.playerId);
        buf.writeEnum(msg.state);
        buf.writeVarInt(msg.actionCount);
        buf.writeBoolean(msg.animationId != null);
        if (msg.animationId != null) buf.writeResourceLocation(msg.animationId);
    }

    public static SMeleeAnimationSync decode(FriendlyByteBuf buf) {
        int playerId = buf.readInt();
        MeleeAction state = buf.readEnum(MeleeAction.class);
        int actionCount = buf.readVarInt();
        ResourceLocation animationId = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new SMeleeAnimationSync(playerId, state, actionCount, animationId);
    }

    public static void handle(SMeleeAnimationSync msg, IPayloadContext context) {
        context.enqueueWork(() -> onClientReceive(msg));
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientReceive(SMeleeAnimationSync msg) {
        NeoForge.EVENT_BUS.post(
                new MeleePreAttackEvent(msg.playerId, msg.state, msg.actionCount, msg.animationId)
        );
    }
}
