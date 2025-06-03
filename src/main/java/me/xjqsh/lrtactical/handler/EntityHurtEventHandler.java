package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.init.ModEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityHurtEventHandler {

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            var effect = event.getEntity().getEffect(ModEffects.FLAMMABLE.get());
            if (effect != null) {
                // 如果有火焰效果，增加伤害
                // 0级为200%，此后每级增加50%
                float additionalDamage = 2.0f + effect.getAmplifier() * 0.5f;
                event.setAmount(event.getAmount() * additionalDamage);
            }
        }
    }
}
