package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

public class CCancelToggleConsumableUse implements CustomPacketPayload {
    public static final Type<CCancelToggleConsumableUse> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "cancel_toggle_consumable_use"));
    public static final StreamCodec<FriendlyByteBuf, CCancelToggleConsumableUse> STREAM_CODEC =
            StreamCodec.of((buf, message) -> message.encode(buf), CCancelToggleConsumableUse::new);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public CCancelToggleConsumableUse() {
    }

    public CCancelToggleConsumableUse(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(CCancelToggleConsumableUse message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) {
                return;
            }

            ItemStack useItem = player.getUseItem();
            if (useItem.getItem() instanceof IConsumable && LrTacticalAPI.getConsumableIndex(useItem)
                    .map(index -> index.getData().isToggleUse())
                    .orElse(false)) {
                // 停止使用但不触发完成效果
                player.stopUsingItem();
            }
        });
    }
}
