package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.client.audio.ICustomSoundSupplier;
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

public record SCustomSound(
        SoundType type,
        ResourceLocation id,
        String key,
        Vec3 pos,
        float volume,
        float pitch
) {
    public enum SoundType {
        MELEE, THROWABLE
    }

    public static void encode(SCustomSound message, FriendlyByteBuf buf) {
        buf.writeEnum(message.type());
        buf.writeResourceLocation(message.id());
        buf.writeUtf(message.key());
        buf.writeDouble(message.pos().x);
        buf.writeDouble(message.pos().y);
        buf.writeDouble(message.pos().z);
        buf.writeFloat(message.volume());
        buf.writeFloat(message.pitch());
    }

    public static SCustomSound decode(FriendlyByteBuf buf) {
        return new SCustomSound(
                buf.readEnum(SoundType.class),
                buf.readResourceLocation(),
                buf.readUtf(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(SCustomSound message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SCustomSound message) {
        Player player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }
        ICustomSoundSupplier supplier = switch (message.type()) {
            case MELEE -> LrTacticalAPI.getMeleeDisplay(message.id()).orElse(null);
            case THROWABLE -> LrTacticalAPI.getThrowableDisplay(message.id()).orElse(null);
            default -> null;
        };
        if (supplier != null) {
            ResourceLocation soundLocation = supplier.getSound(message.key());
            if (soundLocation != null) {
                level.playLocalSound(
                        message.pos.x, message.pos.y, message.pos.z,
                        SoundEvent.createVariableRangeEvent(soundLocation),
                        SoundSource.PLAYERS, message.volume, message.pitch, false
                );
            }
        }
    }
}
