package me.xjqsh.lrtactical.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import java.util.Set;


public class BurnedEffect extends MobEffect {

    public BurnedEffect(int pColor)
    {
        super(MobEffectCategory.HARMFUL, pColor);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance instance) {
        cures.clear();
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        // 延长着火时间
        if (pLivingEntity.isOnFire()) {
            int r = pLivingEntity.getRemainingFireTicks();
            if (r < 40) {
                pLivingEntity.setRemainingFireTicks(r + 40);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
        return duration % 20 == 0;
    }

}
