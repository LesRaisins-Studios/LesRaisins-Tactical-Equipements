package me.xjqsh.lrtactical.capability;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.function.BiConsumer;

@AutoRegisterCapability
public class CombatProperties {
    public static final ResourceLocation ID = new ResourceLocation(EquipmentMod.MOD_ID, "combat_data");

    private ItemStack lastItem = ItemStack.EMPTY;
    private final Player entity;
    private int coolDownTick = 0;
    private int lastMaxTick = 0;
    private int lastSelected = 0;

    private BiConsumer<ItemStack, MeleeAction> delayedAction = null;

    public CombatProperties(Player entity) {
        this.entity = entity;
    }

    /**
     * 获取物品的攻击/使用冷却时间<br/>
     * 此数值通过约定在服务端与客户端之间同步，可能不完全一致
     */
    public int getCoolDownTick() {
        return coolDownTick;
    }

    public int getLastMaxTick() {
        return lastMaxTick;
    }

    public void setCoolDownTick(int coolDownTick) {
        this.coolDownTick = coolDownTick;
    }

    public void tick() {
        if (entity.getMainHandItem().getItem() instanceof ICustomItem customItem){
            if (lastSelected != entity.getInventory().selected) {
                lastSelected = entity.getInventory().selected;
                reset(customItem);
            } else if (!customItem.isSame(lastItem, entity.getMainHandItem())) {
                reset(customItem);
            }
        } else if (!ItemStack.matches(lastItem, entity.getMainHandItem())) {
            lastItem = entity.getMainHandItem();
        }
        if (coolDownTick > 0) {
            coolDownTick--;
        }
    }

    public void reset(ICustomItem customItem) {
        lastItem = entity.getMainHandItem();
        coolDownTick = customItem.getDrawTime(entity.getMainHandItem());
        lastMaxTick = coolDownTick;
    }

    public boolean attack(MeleeAction action) {
        ItemStack stack = entity.getMainHandItem();
        if (entity.getMainHandItem().getItem() instanceof IMeleeWeapon weapon && coolDownTick <= 0) {
            coolDownTick = weapon.getAttackCoolDown(stack, action);
            lastMaxTick = coolDownTick;
            if (!entity.level().isClientSide()) {
                weapon.attack(entity, entity.getMainHandItem());
            }
            return true;
        }
        return false;
    }
}
