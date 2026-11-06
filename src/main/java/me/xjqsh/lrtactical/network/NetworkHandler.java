package me.xjqsh.lrtactical.network;

import me.xjqsh.lrtactical.network.message.*;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String VERSION = "0.4.4";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);

        // Client -> Server
        registrar.playToServer(
                CMeleeAttackRequest.TYPE,
                CMeleeAttackRequest.STREAM_CODEC,
                CMeleeAttackRequest::handle);
        registrar.playToServer(
                CPrepareMeleeAttack.TYPE,
                CPrepareMeleeAttack.STREAM_CODEC,
                CPrepareMeleeAttack::handle);

        // Server -> Client
        registrar.playToClient(
                SPackSyncMessage.TYPE, SPackSyncMessage.STREAM_CODEC, SPackSyncMessage::handle);
        registrar.playToClient(
                SCustomCoolDownMessage.TYPE,
                SCustomCoolDownMessage.STREAM_CODEC,
                SCustomCoolDownMessage::handle);
        registrar.playToClient(SCustomSound.TYPE, SCustomSound.STREAM_CODEC, SCustomSound::handle);
        registrar.playToClient(SShieldShake.TYPE, SShieldShake.STREAM_CODEC, SShieldShake::handle);
        registrar.playToClient(
                SShieldDisable.TYPE, SShieldDisable.STREAM_CODEC, SShieldDisable::handle);
        registrar.playToClient(
                SShakeScreenMessage.TYPE,
                SShakeScreenMessage.STREAM_CODEC,
                SShakeScreenMessage::handle);
        registrar.playToClient(
                SSplashParticle.TYPE, SSplashParticle.STREAM_CODEC, SSplashParticle::handle);
        registrar.playToClient(
                SResetMeleeSyncMessage.TYPE,
                SResetMeleeSyncMessage.STREAM_CODEC,
                SResetMeleeSyncMessage::handle);
        registrar.playToClient(
                SMeleeAnimationSync.TYPE,
                SMeleeAnimationSync.STREAM_CODEC,
                SMeleeAnimationSync::handle);
        // Client -> Server
        registrar.playToServer(
                CCancelToggleConsumableUse.TYPE,
                CCancelToggleConsumableUse.STREAM_CODEC,
                CCancelToggleConsumableUse::handle);
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendToClientPlayer(CustomPacketPayload message, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }

    /**
     * 发送给所有监听此实体的玩家
     */
    public static void sendToTrackingEntityAndSelf(
            Entity centerEntity, CustomPacketPayload message) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(centerEntity, message);
    }

    public static void sendToAllPlayers(CustomPacketPayload message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    public static void sendToTrackingEntity(
            CustomPacketPayload message, final Entity centerEntity) {
        PacketDistributor.sendToPlayersTrackingEntity(centerEntity, message);
    }

    public static void sendToDimension(CustomPacketPayload message, final Entity centerEntity) {
        Level level = centerEntity.level();
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, message);
        }
    }

    public static void sendToNearbyPlayers(
            CustomPacketPayload message, Level level, Vec3 pos, double radius) {
        sendToNearbyPlayers(message, level, pos, radius, null);
    }

    public static void sendToNearbyPlayers(
            CustomPacketPayload message,
            Level level,
            Vec3 pos,
            double radius,
            ServerPlayer excluded) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(
                    serverLevel, excluded, pos.x, pos.y, pos.z, radius, message);
        }
    }
}
