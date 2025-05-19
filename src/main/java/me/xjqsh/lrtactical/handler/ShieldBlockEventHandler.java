package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.item.ShieldItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShieldBlockEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity livingEntity = event.getEntity();
        ItemStack stack = livingEntity.getMainHandItem();
        if (stack.getItem() instanceof ShieldItem shieldItem) {
            stack.hurt((int)event.getBlockedDamage(), livingEntity.level().random,
                    livingEntity instanceof ServerPlayer player ? player : null);
        }
    }
}
