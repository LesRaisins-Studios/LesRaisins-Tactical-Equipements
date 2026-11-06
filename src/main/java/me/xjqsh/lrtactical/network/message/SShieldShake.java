package me.xjqsh.lrtactical.network.message;

import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.item.FlashShieldItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

public record SShieldShake() implements CustomPacketPayload {
    public static final Type<SShieldShake> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lrtactical", "s_shield_shake"));
    public static final StreamCodec<FriendlyByteBuf, SShieldShake> STREAM_CODEC =
            StreamCodec.of((buf, message) -> encode(message, buf), SShieldShake::decode);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(SShieldShake message, FriendlyByteBuf buf) {
    }

    public static SShieldShake decode(FriendlyByteBuf buf) {
        return new SShieldShake();
    }

    public static void handle(SShieldShake message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SShieldShake message) {
        Player player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof FlashShieldItem) {
            if (IClientItemExtensions.of(player.getMainHandItem()).getCustomRenderer() instanceof AnimateGeoItemRenderer<?,?> renderer) {
                renderer.triggerAnimation(player.getMainHandItem(), "normal_shake");
            }
        }
    }
}
