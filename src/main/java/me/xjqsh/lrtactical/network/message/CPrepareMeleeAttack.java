package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CPrepareMeleeAttack(
        MeleeAction action,
        Vec3 origin,
        Vec3 direction
) {
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

    // 在客户端执行相应攻击指令后发送
    // 收到此包后，服务端应准备进行近战判定，准备监听客户端发来的实际延迟攻击包
    // 如果攻击延迟为0则直接进行攻击
    // 同时，开始进入攻击冷却读条
    public static void handle(CPrepareMeleeAttack message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                player.getCapability(CombatPropertiesProvider.CAPABILITY).ifPresent(cap -> {
                    cap.preAttack(message.action, message.origin, message.direction);
                });
            });
        }
        context.setPacketHandled(true);
    }
}
