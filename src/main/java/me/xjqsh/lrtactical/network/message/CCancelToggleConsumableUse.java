package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CCancelToggleConsumableUse {
    public CCancelToggleConsumableUse() {
    }

    public CCancelToggleConsumableUse(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
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
        context.setPacketHandled(true);
    }
}
