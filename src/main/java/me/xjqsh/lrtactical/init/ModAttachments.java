package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EquipmentMod.MOD_ID);
    public static final Supplier<AttachmentType<CombatProperties>> COMBAT =
            TYPES.register(
                    "combat_data",
                    () ->
                            AttachmentType.builder(holder -> new CombatProperties((Player) holder))
                                    .build());
    public static final Supplier<AttachmentType<CustomItemCoolDowns>> COOLDOWNS =
            TYPES.register(
                    "custom_cooldown",
                    () ->
                            AttachmentType.builder(
                                            holder -> new CustomItemCoolDowns((Player) holder))
                                    .build());

    private ModAttachments() {
    }
}
