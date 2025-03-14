package me.xjqsh.lrtactical.compat.cloth.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.xjqsh.lrtactical.config.ClientConfig;
import net.minecraft.network.chat.Component;

public class BasicClothConfig {
    public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        root.getOrCreateCategory(Component.translatable("config.lrtactical.effect"))
            .addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("config.lrtactical.effect.blackflash"), ClientConfig.BLACK_FLASH.get())
                    .setDefaultValue(false)
                    .setTooltip(Component.translatable("config.lrtactical.effect.blackflash.desc"))
                    .setSaveConsumer(ClientConfig.BLACK_FLASH::set)
                    .build()
            );
    }
}
