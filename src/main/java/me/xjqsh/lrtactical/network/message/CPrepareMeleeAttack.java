package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

public record CPrepareMeleeAttack(
        MeleeAction action,
        Vec3 origin,
        Vec3 direction
) implements CustomPacketPayload {
    public static final Type<CPrepareMeleeAttack> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "c_prepare_melee_attack"));
    public static final StreamCodec<FriendlyByteBuf, CPrepareMeleeAttack> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), CPrepareMeleeAttack::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(CPrepareMeleeAttack message, FriendlyByteBuf buf) {
        buf.writeEnum(message.action);
        buf.writeDouble(message.origin.x);
        buf.writeDouble(message.origin.y);
        buf.writeDouble(message.origin.z);
        buf.writeDouble(message.direction.x);
        buf.writeDouble(message.direction.y);
        buf.writeDouble(message.direction.z);
    }

    public static CPrepareMeleeAttack decode(FriendlyByteBuf buf) {
        return new CPrepareMeleeAttack(
                buf.readEnum(MeleeAction.class),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        );
    }

    public static void handle(CPrepareMeleeAttack message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) {
                return;
            }
            CombatProperties.get(player).ifPresent(cap -> {
                if (cap.preAttack(message.action, message.origin, message.direction)
                        && player.getMainHandItem().getItem() instanceof IMeleeWeapon weapon) {
                    var animationId = weapon.getId(player.getMainHandItem());
                    int actionCount = cap.getActionCount(message.action);
                    NetworkHandler.sendToTrackingEntityAndSelf(
                            player,
                            new SMeleeAnimationSync(player.getId(), message.action, actionCount, animationId)
                    );
                }
            });
        });
    }
}
