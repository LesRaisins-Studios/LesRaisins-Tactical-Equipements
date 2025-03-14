package me.xjqsh.lrtactical.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class HarmfulEffect extends MobEffect {
    public HarmfulEffect(int pColor) {
        super(MobEffectCategory.HARMFUL, pColor);
    }
}
