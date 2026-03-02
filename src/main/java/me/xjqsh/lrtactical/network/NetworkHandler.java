package me.xjqsh.lrtactical.network;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.network.message.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public class NetworkHandler {
    private static final String VERSION = "0.3.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(EquipmentMod.MOD_ID, "network"),
            () -> VERSION, it -> it.equals(VERSION), it -> it.equals(VERSION));

    private static final AtomicInteger ID_COUNT = new AtomicInteger(1);

    public static void init() {
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SPackSyncMessage.class,
                SPackSyncMessage::encode,
                SPackSyncMessage::decode,
                SPackSyncMessage::handle
        );
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SCustomCoolDownMessage.class,
                SCustomCoolDownMessage::encode,
                SCustomCoolDownMessage::decode,
                SCustomCoolDownMessage::handle
        );
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SCustomSound.class,
                SCustomSound::encode,
                SCustomSound::decode,
                SCustomSound::handle
        );

        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                CMeleeAttackRequest.class,
                CMeleeAttackRequest::encode,
                CMeleeAttackRequest::decode,
                CMeleeAttackRequest::handle
        );
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                CPrepareMeleeAttack.class,
                CPrepareMeleeAttack::encode,
                CPrepareMeleeAttack::decode,
                CPrepareMeleeAttack::handle
        );

        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SShieldShake.class,
                SShieldShake::encode,
                SShieldShake::decode,
                SShieldShake::handle
        );
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SShieldDisable.class,
                SShieldDisable::encode,
                SShieldDisable::decode,
                SShieldDisable::handle
        );
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SShakeScreenMessage.class,
                SShakeScreenMessage::encode,
                SShakeScreenMessage::decode,
                SShakeScreenMessage::handle
        );

        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SSplashParticle.class,
                SSplashParticle::encode,
                SSplashParticle::decode,
                SSplashParticle::handle
        );

        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SMeleeAnimationSync.class,
                SMeleeAnimationSync::encode,
                SMeleeAnimationSync::decode,
                SMeleeAnimationSync::handle
        );
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(),
                SThrowableAnimationSync.class,
                SThrowableAnimationSync::encode,
                SThrowableAnimationSync::decode,
                SThrowableAnimationSync::handle
        );
    }

    public static void sendToClientPlayer(Object message, Player player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), message);
    }

    /**
     * 发送给所有监听此实体的玩家
     */
    public static void sendToTrackingEntityAndSelf(Entity centerEntity, Object message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> centerEntity), message);
    }

    public static void sendToAllPlayers(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendToTrackingEntity(Object message, final Entity centerEntity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> centerEntity), message);
    }

    public static void sendToDimension(Object message, final Entity centerEntity) {
        ResourceKey<Level> dimension = centerEntity.level().dimension();
        CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
    }

    public static void sendToNearbyPlayers(Object message, Level level, Vec3 position, double radius) {
        CHANNEL.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(
                position.x, position.y, position.z, radius, level.dimension()
        )), message);
    }
}
