package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record CMeleeAttackRequest(
        MeleeAction action,
        int[] entityIds
) {
    public CMeleeAttackRequest(MeleeAction action, List<Entity> entities) {
        this(action, toList(entities));
    }

    private static int[] toList(List<Entity> entities) {
        return entities.stream().mapToInt(Entity::getId).toArray();
    }

    public static void encode(CMeleeAttackRequest message, FriendlyByteBuf buf) {
        buf.writeEnum(message.action);
        buf.writeVarIntArray(message.entityIds);
    }

    public static CMeleeAttackRequest decode(FriendlyByteBuf buf) {
        MeleeAction action = buf.readEnum(MeleeAction.class);
        int[] ids = buf.readVarIntArray();
        return new CMeleeAttackRequest(action, ids);
    }

    public static void handle(CMeleeAttackRequest message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }

                List<Entity> entities = new ArrayList<>();
                for (int entityId : message.entityIds()) {
                    Entity entity = player.level().getEntity(entityId);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }

                player.getCapability(CombatPropertiesProvider.CAPABILITY).ifPresent(cap -> {
                    cap.postAttack(message.action, entities);
                });
            });
        }
        context.setPacketHandled(true);
    }
}