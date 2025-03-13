package me.xjqsh.lrtactical.capability;

import com.google.common.collect.Maps;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SCustomCoolDownMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Iterator;
import java.util.Map;

// 一个原版物品冷却实现的翻版，不过key是ResourceLocation，用来给复用物品id的物品设置冷却
@AutoRegisterCapability
public class CustomItemCoolDowns {
    private final Player player;
    public Map<ResourceLocation, CustomItemCoolDowns.CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public CustomItemCoolDowns(Player player) {
        this.player = player;
    }

    public boolean isOnCooldown(ResourceLocation id) {
        return this.getCooldownPercent(id, 0.0F) > 0.0F;
    }

    public float getCooldownPercent(ResourceLocation id, float pPartialTicks) {
        CustomItemCoolDowns.CooldownInstance itemcooldowns$cooldowninstance = this.cooldowns.get(id);
        if (itemcooldowns$cooldowninstance != null) {
            float f = (float)(itemcooldowns$cooldowninstance.endTime - itemcooldowns$cooldowninstance.startTime);
            float f1 = (float)itemcooldowns$cooldowninstance.endTime - ((float)this.tickCount + pPartialTicks);
            return Mth.clamp(f1 / f, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        ++this.tickCount;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<ResourceLocation, CustomItemCoolDowns.CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();

            while(iterator.hasNext()) {
                Map.Entry<ResourceLocation, CustomItemCoolDowns.CooldownInstance> entry = iterator.next();
                if ((entry.getValue()).endTime <= this.tickCount) {
                    iterator.remove();
                    this.onCooldownEnded(entry.getKey());
                }
            }
        }

    }

    public void addCooldown(ResourceLocation id, int pTicks) {
        this.cooldowns.put(id, new CustomItemCoolDowns.CooldownInstance(this.tickCount, this.tickCount + pTicks));
        this.onCooldownStarted(id, pTicks);
    }

    public void removeCooldown(ResourceLocation id) {
        this.cooldowns.remove(id);
        this.onCooldownEnded(id);
    }

    protected void onCooldownStarted(ResourceLocation id, int pTicks) {
        if (!player.level().isClientSide()) {
            NetworkHandler.sendToClientPlayer(new SCustomCoolDownMessage(id, pTicks), player);
        }
    }

    protected void onCooldownEnded(ResourceLocation id) {
        if (!player.level().isClientSide()) {
            NetworkHandler.sendToClientPlayer(new SCustomCoolDownMessage(id, 0), player);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public static class CooldownInstance {
        final int startTime;
        final int endTime;

        CooldownInstance(int pStartTime, int pEndTime) {
            this.startTime = pStartTime;
            this.endTime = pEndTime;
        }
    }
}
