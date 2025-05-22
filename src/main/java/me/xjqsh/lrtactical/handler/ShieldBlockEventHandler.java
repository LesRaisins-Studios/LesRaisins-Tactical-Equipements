package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.item.FlashShieldItem;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SShieldShake;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShieldBlockEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity livingEntity = event.getEntity();
        ItemStack stack = livingEntity.getMainHandItem();
        if (stack.getItem() instanceof FlashShieldItem flashShieldItem) {
            int restDurability = stack.getMaxDamage() - stack.getDamageValue();
            restDurability = Math.max(0, restDurability);

            float blockedDamage = Math.min(event.getBlockedDamage(), restDurability);
            event.setBlockedDamage(blockedDamage);
            stack.hurtAndBreak((int) blockedDamage, livingEntity, (e)->{});

            if (blockedDamage >= 1) {
                if (livingEntity instanceof ServerPlayer serverPlayer) {
                    // 发送消息到客户端，触发动画
                    NetworkHandler.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new SShieldShake()
                    );
                }
            }
        }
    }
}
