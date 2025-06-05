package me.xjqsh.lrtactical.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static ForgeConfigSpec.BooleanValue GRENADE_EXPLOSION_BLOCK_DAMAGE;
    public static ForgeConfigSpec.BooleanValue MELEE_ITEM_CONSUME_DURABILITY;


    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("grenade");
        GRENADE_EXPLOSION_BLOCK_DAMAGE = builder
                .comment("Whether grenade explosion can damage blocks (if it can)")
                .define("grenadeExplosionBlockDamage", true);
        builder.pop();

        builder.push("melee");
        MELEE_ITEM_CONSUME_DURABILITY = builder
                .comment("Whether melee items consume durability on attack")
                .define("meleeItemConsumeDurability", true);
        builder.pop();


        return builder.build();
    }
}
