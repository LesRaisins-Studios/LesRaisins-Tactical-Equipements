package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import me.xjqsh.lrtactical.config.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record CMeleeAttackRequest(MeleeAction action) {
    public static void encode(CMeleeAttackRequest message, FriendlyByteBuf buf) {
        buf.writeEnum(message.action);
    }

    public static CMeleeAttackRequest decode(FriendlyByteBuf buf) {
        MeleeAction action = buf.readEnum(MeleeAction.class);
        return new CMeleeAttackRequest(action);
    }

    public static void handle(CMeleeAttackRequest message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                ItemStack stack = player.getMainHandItem();
                /*
                  改为由服务器去判断实体进行攻击，对于像ModelEngine插件这种不存在ID的生物会有帮助
                 */
                if (stack.getItem() instanceof IMeleeWeapon weapon && weapon.isSame(stack, player.getMainHandItem())) {
                    List<Entity> entities = weapon.collectTargets(player, stack, message.action, player.getEyePosition(), player.getLookAngle());
                    if (entities.size() > ServerConfig.MELEE_MAX_TARGET_PER_PACKET.get()) {
                        EquipmentMod.LOGGER.info(
                                "Player {} tried to attack too many entities at once: {}! Ignoring.",
                                player.getName().getString(),
                                entities.size()
                        );
                        return;
                    }
                    player.getCapability(CombatPropertiesProvider.CAPABILITY).ifPresent(cap -> {
                        cap.postAttack(message.action, entities);
                    });
                }

            });
        }
        context.setPacketHandled(true);
    }
}