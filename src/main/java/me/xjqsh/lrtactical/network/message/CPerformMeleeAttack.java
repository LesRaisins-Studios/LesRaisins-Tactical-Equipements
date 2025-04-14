package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CPerformMeleeAttack(
        MeleeAction action,
        Vec3 origin,
        Vec3 direction
) {
    public static void encode(CPerformMeleeAttack message, FriendlyByteBuf buf) {
        buf.writeEnum(message.action);
        buf.writeDouble(message.origin.x);
        buf.writeDouble(message.origin.y);
        buf.writeDouble(message.origin.z);
        buf.writeDouble(message.direction.x);
        buf.writeDouble(message.direction.y);
        buf.writeDouble(message.direction.z);
    }

    public static CPerformMeleeAttack decode(FriendlyByteBuf buf) {
        return new CPerformMeleeAttack(
                buf.readEnum(MeleeAction.class),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        );
    }

    // 在客户端完成攻击读条后发送，告知服务器进行实际攻击判定
    // 包含客户端当时的摄像机位置和攻击角度
    public static void handle(CPerformMeleeAttack message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                player.getCapability(CombatPropertiesProvider.CAPABILITY).ifPresent(cap -> {
                    cap.postAttack(message.action, message.origin, message.direction);
                });
            });
        }
        context.setPacketHandled(true);
    }
}
