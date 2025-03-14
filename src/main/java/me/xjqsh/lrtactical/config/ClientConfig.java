package me.xjqsh.lrtactical.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec.BooleanValue BLACK_FLASH;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Use black overlay instead of white when blinded by flashbang");
        BLACK_FLASH = builder.define("blackFlash", false);
        return builder.build();
    }
}
