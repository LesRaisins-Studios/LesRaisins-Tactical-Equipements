package me.xjqsh.lrtactical.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Packet to sync throwable item animations to other players
 */
public class SThrowableAnimationSync {
    private final int playerId;
    private final ThrowableState state;
    private final ResourceLocation animationId;

    public SThrowableAnimationSync(int playerId, ThrowableState state, @Nullable ResourceLocation animationId) {
        this.playerId = playerId;
        this.state = state;
        this.animationId = animationId;
    }

    public static void encode(SThrowableAnimationSync message, FriendlyByteBuf buf) {
        buf.writeInt(message.playerId);
        buf.writeEnum(message.state);
        buf.writeBoolean(message.animationId != null);
        if (message.animationId != null) {
            buf.writeResourceLocation(message.animationId);
        }
    }

    public static SThrowableAnimationSync decode(FriendlyByteBuf buf) {
        int playerId = buf.readInt();
        ThrowableState state = buf.readEnum(ThrowableState.class);
        ResourceLocation animationId = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new SThrowableAnimationSync(playerId, state, animationId);
    }

    public static void handle(SThrowableAnimationSync message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                // Post event to be handled by PlayerAnimator compat if loaded
                MinecraftForge.EVENT_BUS.post(new ClientEvent(message.playerId, message.state, message.animationId));
            });
        }
        context.setPacketHandled(true);
    }

    public enum ThrowableState {
        IDLE, PREPARE, THROW
    }

    /**
     * Client-side event for animation playback
     */
    @OnlyIn(Dist.CLIENT)
    public static class ClientEvent extends Event {
        private final int playerId;
        private final ThrowableState state;
        private final ResourceLocation animationId;

        public ClientEvent(int playerId, ThrowableState state, @Nullable ResourceLocation animationId) {
            this.playerId = playerId;
            this.state = state;
            this.animationId = animationId;
        }

        public int getPlayerId() {
            return playerId;
        }

        public ThrowableState getState() {
            return state;
        }

        @Nullable
        public ResourceLocation getAnimationId() {
            return animationId;
        }
    }
}
