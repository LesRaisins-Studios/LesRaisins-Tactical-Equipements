package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.init.ModEnchantment;
import me.xjqsh.lrtactical.util.VectorUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CriticalHitEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        int level = player.getMainHandItem().getEnchantmentLevel(ModEnchantment.BACKSTAB.get());
        if (level > 0) {
            Entity target = event.getTarget();
            Vec3 origin = player.getEyePosition();
            Vec3 positionVector = target.position().add(0, target.getBbHeight() / 2F, 0).subtract(origin);
            positionVector = new Vec3(positionVector.x, 0, positionVector.z).normalize();
            // 检查是否从背后攻击
            Vec3 targetForward = target.getForward();
            double angle = VectorUtil.angleBetween(positionVector, targetForward);
            if (angle <= 45) {
                event.setResult(Event.Result.ALLOW);
                event.setDamageModifier(event.getOldDamageModifier() + level * 0.25f);
            }
        }
    }
}
