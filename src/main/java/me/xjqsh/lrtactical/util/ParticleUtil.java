package me.xjqsh.lrtactical.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ParticleUtil {
    public static <T extends ParticleOptions> void sendParticle(ServerLevel level, T particle, double x, double y, double z, int count,
                                                                double xOffset, double yOffset, double zOffset, double speed, boolean force) {
        for (ServerPlayer serverPlayer : level.players()) {
            sendParticle(level, particle, x, y, z, count, xOffset, yOffset, zOffset, speed, force, serverPlayer);
        }
    }

    public static <T extends ParticleOptions> void sendParticle(ServerLevel level, T particle, double x, double y, double z, int count,
                                                                double xOffset, double yOffset, double zOffset, double speed, boolean force, ServerPlayer viewer) {
        level.sendParticles(viewer, particle, force, x, y, z, count, xOffset, yOffset, zOffset, speed);
    }
}
