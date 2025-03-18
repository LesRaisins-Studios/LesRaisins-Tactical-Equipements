package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SThrowableSound(
        ResourceLocation id,
        String key,
        Vec3 pos,
        float volume,
        float pitch
) {
    public static void encode(SThrowableSound message, FriendlyByteBuf buf) {
        buf.writeResourceLocation(message.id());
        buf.writeUtf(message.key());
        buf.writeDouble(message.pos().x);
        buf.writeDouble(message.pos().y);
        buf.writeDouble(message.pos().z);
        buf.writeFloat(message.volume());
        buf.writeFloat(message.pitch());
    }

    public static SThrowableSound decode(FriendlyByteBuf buf) {
        return new SThrowableSound(
                buf.readResourceLocation(),
                buf.readUtf(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(SThrowableSound message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SThrowableSound message) {
        Player player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }
        LrTacticalAPI.getThrowableDisplay(message.id).ifPresent(display -> {
            ResourceLocation rl = display.getSounds().get(message.key);
            if (rl != null) {
                level.playLocalSound(
                        message.pos.x, message.pos.y, message.pos.z,
                        SoundEvent.createVariableRangeEvent(rl),
                        SoundSource.BLOCKS, 6.0F, 1.0F, false
                );
            }
        });
    }
}
