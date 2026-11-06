package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.config.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record CMeleeAttackRequest(
        MeleeAction action,
        int actionCount,
        int[] entityIds
) implements CustomPacketPayload {
    public static final Type<CMeleeAttackRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "c_melee_attack_request"));
    public static final StreamCodec<FriendlyByteBuf, CMeleeAttackRequest> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), CMeleeAttackRequest::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public CMeleeAttackRequest(MeleeAction action, int actionCount, List<Entity> entities) {
        this(action, actionCount, toList(entities));
    }

    private static int[] toList(List<Entity> entities) {
        return entities.stream().mapToInt(Entity::getId).limit(ServerConfig.MELEE_MAX_TARGET_PER_PACKET.get()).toArray();
    }

    public static void encode(CMeleeAttackRequest message, FriendlyByteBuf buf) {
        buf.writeEnum(message.action);
        buf.writeVarInt(message.actionCount);
        buf.writeVarIntArray(message.entityIds);
    }

    public static CMeleeAttackRequest decode(FriendlyByteBuf buf) {
        MeleeAction action = buf.readEnum(MeleeAction.class);
        int actionCount = buf.readVarInt();
        int[] ids = buf.readVarIntArray();
        return new CMeleeAttackRequest(action, actionCount, ids);
    }

    public static void handle(CMeleeAttackRequest message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) {
                return;
            }

            if (message.entityIds.length > ServerConfig.MELEE_MAX_TARGET_PER_PACKET.get()) {
                EquipmentMod.LOGGER.info(
                        "Player {} tried to attack too many entities at once: {}! Ignoring.",
                        player.getName().getString(),
                        message.entityIds.length
                );
                return;
            }

            List<Entity> entities = new ArrayList<>();
            for (int entityId : message.entityIds()) {
                Entity entity = player.level().getEntity(entityId);
                if (entity != null) {
                    entities.add(entity);
                }
            }

            CombatProperties.get(player).ifPresent(cap -> {
                cap.postAttack(message.action, message.actionCount, entities);
            });
        });
    }
}
