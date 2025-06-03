package me.xjqsh.lrtactical.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class BurnedEffect extends MobEffect {

    public BurnedEffect(int pColor)
    {
        super(MobEffectCategory.HARMFUL, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        // 延长着火时间
        if (pLivingEntity.isOnFire()) {
            int r = pLivingEntity.getRemainingFireTicks();
            if (r < 40) {
                pLivingEntity.setRemainingFireTicks(r + 40);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){
        return duration % 20 == 0;
    }

}
