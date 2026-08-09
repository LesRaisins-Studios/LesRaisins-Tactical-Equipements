package me.xjqsh.lrtactical.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

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

        List<Pair<Attribute, AttributeModifier>> attributeModifiers = new ArrayList<>();
        for (EffectWithChance effectWithChance : effects) {
            MobEffectInstance effectInstance = effectWithChance.effect();
            MutableComponent effectName = Component.translatable(effectInstance.getDescriptionId());
            MobEffect effect = effectInstance.getEffect();
            for (var entry : effect.getAttributeModifiers().entrySet()) {
                AttributeModifier modifier = entry.getValue();
                AttributeModifier scaledModifier = new AttributeModifier(
                        modifier.getName(),
                        effect.getAttributeModifierValue(effectInstance.getAmplifier(), modifier),
                        modifier.getOperation()
                );
                attributeModifiers.add(new Pair<>(entry.getKey(), scaledModifier));
            }

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
                        MobEffectUtil.formatDuration(effectInstance, durationFactor),
                        formatChance(effectWithChance.chance())
                );
            } else if (hasDuration) {
                effectName = Component.translatable(
                        "potion.withDuration",
                        effectName,
                        MobEffectUtil.formatDuration(effectInstance, durationFactor)
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
        for (Pair<Attribute, AttributeModifier> attributeModifier : attributeModifiers) {
            AttributeModifier modifier = attributeModifier.getSecond();
            double amount = modifier.getAmount();
            double displayAmount = amount;
            if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
                    || modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                displayAmount *= 100.0D;
            }

            if (amount > 0.0D) {
                tooltips.add(Component.translatable(
                        "attribute.modifier.plus." + modifier.getOperation().toValue(),
                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount),
                        Component.translatable(attributeModifier.getFirst().getDescriptionId())
                ).withStyle(ChatFormatting.BLUE));
            } else if (amount < 0.0D) {
                tooltips.add(Component.translatable(
                        "attribute.modifier.take." + modifier.getOperation().toValue(),
                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(-displayAmount),
                        Component.translatable(attributeModifier.getFirst().getDescriptionId())
                ).withStyle(ChatFormatting.RED));
            }
        }
    }

    private static Component formatChance(float chance) {
        return Component.literal(String.format(Locale.ROOT, "%.0f%%", chance * 100.0F));
    }
}
