package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.event.MeleePreAttackEvent;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SMeleeAnimationSync {
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

    public static void handle(SMeleeAnimationSync msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> onClientReceive(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientReceive(SMeleeAnimationSync msg) {
        MinecraftForge.EVENT_BUS.post(
                new MeleePreAttackEvent(msg.playerId, msg.state, msg.actionCount, msg.animationId)
        );
    }
}
