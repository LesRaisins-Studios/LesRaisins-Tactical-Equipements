package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.init.ModEffects;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class EntityHurtEventHandler {

    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            var effect = event.getEntity().getEffect(ModEffects.FLAMMABLE);
            if (effect != null) {
                // 如果有火焰效果，增加伤害
                // 0级为200%，此后每级增加50%
                float additionalDamage = 2.0f + effect.getAmplifier() * 0.5f;
                event.setNewDamage(event.getNewDamage() * additionalDamage);
            }
        }
    }
}
