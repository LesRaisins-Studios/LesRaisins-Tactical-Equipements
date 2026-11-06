package me.xjqsh.lrtactical.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {
    public static ModConfigSpec.BooleanValue GRENADE_EXPLOSION_BLOCK_DAMAGE;
    public static ModConfigSpec.BooleanValue MELEE_ITEM_CONSUME_DURABILITY;
    public static ModConfigSpec.IntValue MELEE_IGNORE_INVULNERABLE_TICK_THRESHOLD;

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("grenade");
        GRENADE_EXPLOSION_BLOCK_DAMAGE = builder
                .comment("Whether grenade explosion can damage blocks (if it can)")
                .define("grenadeExplosionBlockDamage", true);
        builder.pop();

        builder.push("melee");
        MELEE_ITEM_CONSUME_DURABILITY = builder
                .comment("Whether melee items consume durability on attack")
                .define("meleeItemConsumeDurability", true);
        MELEE_IGNORE_INVULNERABLE_TICK_THRESHOLD = builder
                .comment("when target's invulnerable tick is less than this value, melee attack will ignore it")
                .defineInRange("meleeIgnoreInvulnerableTickThreshold", 20, 0, 100);
        builder.pop();

        return builder.build();
    }
}
