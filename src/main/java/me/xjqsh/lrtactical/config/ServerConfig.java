package me.xjqsh.lrtactical.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public static ForgeConfigSpec.IntValue FLASH_SHIELD_MAX_DURABILITY;
    public static ForgeConfigSpec.IntValue FLASH_SHIELD_COOLDOWN;

    public static ForgeConfigSpec.DoubleValue CROUCHING_INIT_SPEED_PERCENT;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("flash shield");
        FLASH_SHIELD_MAX_DURABILITY = builder
                .comment("max durability of flash shield")
                .defineInRange("flash_shield_max_durability", 450, 1, 32767);
        FLASH_SHIELD_COOLDOWN = builder
                .comment("cooldown of flash shield in ticks")
                .defineInRange("flash_shield_cooldown", 200, 20, 32767);
        builder.pop();

        builder.push("throwable");
        CROUCHING_INIT_SPEED_PERCENT = builder
                .comment("modifier of initial speed when crouching, 1.0 means no change")
                .defineInRange("flash_shield_cooldown", 0.5f, 0.01, 2);
        builder.pop();

        return builder.build();
    }
}
