package me.xjqsh.lrtactical.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec.BooleanValue BLACK_FLASH;
    public static ForgeConfigSpec.DoubleValue EXPLODE_SCREEN_SHAKE_MULTIPLIER;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Use black overlay instead of white when blinded by flashbang");
        BLACK_FLASH = builder.define("blackFlash", false);
        EXPLODE_SCREEN_SHAKE_MULTIPLIER = builder
                .comment("Screen shake multiplier for explosions, default is 1.0")
                .defineInRange("explodeScreenShakeMultiplier", 1.0, 0.0, 128.0);
        return builder.build();
    }
}
