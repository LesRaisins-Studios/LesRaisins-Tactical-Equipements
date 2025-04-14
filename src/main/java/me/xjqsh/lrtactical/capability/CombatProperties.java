package me.xjqsh.lrtactical.capability;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.CPerformMeleeAttack;
import me.xjqsh.lrtactical.network.message.CPrepareMeleeAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public class CombatProperties {
    public static final ResourceLocation ID = new ResourceLocation(EquipmentMod.MOD_ID, "combat_data");

    private ItemStack lastItem = ItemStack.EMPTY;
    private final Player entity;
    private int coolDownTick = 0;
    private int lastMaxTick = 0;
    private int lastSelected = 0;

    private DelayAttack delayedAction = null;

    private boolean preparingAttack = false;

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
            lastItem = entity.getMainHandItem().copy();
        }
        if (coolDownTick > 0) {
            coolDownTick--;
            if (coolDownTick <= 0) {
                preparingAttack = false;
            }
        }

        if (this.entity.level().isClientSide() && delayedAction != null) {
            if (delayedAction.tick()) {
                delayedAction.perform(entity);
                delayedAction = null;
            }
        }
    }

    public void reset(ICustomItem customItem) {
        lastItem = entity.getMainHandItem().copy();
        coolDownTick = customItem.getDrawTime(entity.getMainHandItem());
        lastMaxTick = coolDownTick;
        preparingAttack = false;
    }

    public boolean preAttack(MeleeAction action, Vec3 origin, Vec3 direction) {
        ItemStack stack = entity.getMainHandItem();
        if (entity.getMainHandItem().getItem() instanceof IMeleeWeapon weapon && coolDownTick <= 0) {
            coolDownTick = weapon.getAttackCoolDown(stack, action);
            lastMaxTick = coolDownTick;
            int delay = weapon.getAttackDelay(entity, stack, action);

            if (!entity.level().isClientSide()) {
                // 服务端，准备进行攻击
                this.preparingAttack = true;
                if (delay == 0){
                    this.postAttack(action, entity.position(), entity.getLookAngle());
                }
            } else {
                // 客户端，通知服务端准备进行攻击判断并安排延迟任务
                NetworkHandler.CHANNEL.sendToServer(new CPrepareMeleeAttack(action, origin, direction));
                if (delay > 0) {
                    this.delayedAction = new DelayAttack(delay, stack, action);
                }
            }
            return true;
        }
        return false;
    }

    public void postAttack(MeleeAction action, Vec3 origin, Vec3 direction) {
        ItemStack stack = entity.getMainHandItem();
        if (!this.preparingAttack) {
            return;
        }
        if (stack.getItem() instanceof IMeleeWeapon weapon) {
            weapon.attack(entity, stack, action, origin, direction);
        }
        preparingAttack = false;
    }

    public static class DelayAttack {
        private int delay;
        private final ItemStack stack;
        private final MeleeAction action;

        DelayAttack(int delay, ItemStack stack, MeleeAction action) {
            this.delay = delay;
            this.action = action;
            this.stack = stack;
        }

        public void perform(Player player) {
            if (stack.getItem() instanceof IMeleeWeapon weapon && weapon.isSame(stack, player.getMainHandItem())) {
                NetworkHandler.CHANNEL.sendToServer(new CPerformMeleeAttack(action, player.getEyePosition(), player.getLookAngle()));
            }
        }

        public boolean tick() {
            delay--;
            return delay <= 0;
        }
    }
}
