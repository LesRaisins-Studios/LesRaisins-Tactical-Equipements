package me.xjqsh.lrtactical.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.util.AttributeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PotionTooltipUtil {
    private PotionTooltipUtil() {
    }

    public record EffectWithChance(MobEffectInstance effect, float chance) {
    }

    public static void addPotionTooltip(List<EffectWithChance> effects, List<Component> tooltips, float durationFactor) {
        if (effects.isEmpty()) {
            return;
        }

        List<Pair<Holder<Attribute>, AttributeModifier>> attributeModifiers = new ArrayList<>();
        for (EffectWithChance effectWithChance : effects) {
            MobEffectInstance effectInstance = effectWithChance.effect();
            MutableComponent effectName = Component.translatable(effectInstance.getDescriptionId());
            Holder<MobEffect> effectHolder = effectInstance.getEffect();
            MobEffect effect = effectHolder.value();
            effect.createModifiers(effectInstance.getAmplifier(), (holder, modifier) ->
                    attributeModifiers.add(new Pair<>(holder, modifier)));

            if (effectInstance.getAmplifier() > 0) {
                effectName = Component.translatable(
                        "potion.withAmplifier",
                        effectName,
                        Component.translatable("potion.potency." + effectInstance.getAmplifier())
                );
            }

            boolean hasDuration = !effectInstance.endsWithin(20);
            boolean hasChance = effectWithChance.chance() < 1.0F;
            if (hasDuration && hasChance) {
                effectName = Component.translatable(
                        "tooltip.lrtactical.consumable.effect.with_duration_and_chance",
                        effectName,
                        MobEffectUtil.formatDuration(effectInstance, durationFactor, 20.0F),
                        formatChance(effectWithChance.chance())
                );
            } else if (hasDuration) {
                effectName = Component.translatable(
                        "potion.withDuration",
                        effectName,
                        MobEffectUtil.formatDuration(effectInstance, durationFactor, 20.0F)
                );
            } else if (hasChance) {
                effectName = Component.translatable(
                        "tooltip.lrtactical.consumable.effect.with_chance",
                        effectName,
                        formatChance(effectWithChance.chance())
                );
            }

            tooltips.add(effectName.withStyle(effect.getCategory().getTooltipFormatting()));
        }

        if (attributeModifiers.isEmpty()) {
            return;
        }

        tooltips.add(CommonComponents.EMPTY);
        tooltips.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
        AttributeUtil.addPotionTooltip(attributeModifiers, tooltips::add);
    }

    private static Component formatChance(float chance) {
        return Component.literal(String.format(Locale.ROOT, "%.0f%%", chance * 100.0F));
    }
}
