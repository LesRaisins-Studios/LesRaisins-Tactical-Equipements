package me.xjqsh.lrtactical.capability;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.CMeleeAttackRequest;
import me.xjqsh.lrtactical.network.message.CPrepareMeleeAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.ArrayList;
import java.util.List;

//todo 临时实现，太丑了，还得改
@AutoRegisterCapability
public class CombatProperties {
    public static final ResourceLocation ID = new ResourceLocation(EquipmentMod.MOD_ID, "combat_data");

    private final List<DelayTask> delayedActions = new ArrayList<>();
    private ItemStack lastItem = ItemStack.EMPTY;
    private final Player entity;
    private int coolDownTick = 0;
    private int lastMaxTick = 0;
    private int lastSelected = 0;
    private int drawingTick = 0;
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
                reset(customItem, lastItem);
            } else if (!customItem.isSame(lastItem, entity.getMainHandItem())) {
                reset(customItem, lastItem);
            }
        } else if (!ItemStack.matches(lastItem, entity.getMainHandItem())) {
            lastItem = entity.getMainHandItem().copy();
        }

        if (coolDownTick > 0) {
            coolDownTick--;
            if (entity.getMainHandItem().getItem() instanceof IMeleeWeapon weapon && !weapon.canSprintingAttack()) {
                entity.setSprinting(false);
            }
            if (coolDownTick <= 0) {
                preparingAttack = false;
            }
        }

        if (this.entity.level().isClientSide()) {
            for (DelayTask task : delayedActions) {
                if (task.tick()) {
                    task.perform(entity);
                }
            }
            delayedActions.removeIf(DelayTask::expired);
        }

        if (drawingTick > 0) {
            drawingTick--;
        }
    }

    public boolean isDrawing() {
        return drawingTick > 0;
    }

    public void reset(ICustomItem customItem, ItemStack last) {
        lastItem = entity.getMainHandItem().copy();
        int newCoolDown = customItem.getDrawTime(entity.getMainHandItem());
        if (last.getItem() instanceof ICustomItem customItem1) {
            newCoolDown += customItem1.getPutAwayTime(last);
        }
        coolDownTick = newCoolDown;
        lastMaxTick = newCoolDown;
        drawingTick = newCoolDown;
        preparingAttack = false;
    }

    public boolean preAttack(MeleeAction action, Vec3 origin, Vec3 direction) {
        ItemStack stack = entity.getMainHandItem();
        if (entity.getMainHandItem().getItem() instanceof IMeleeWeapon weapon && coolDownTick <= 0) {
            if (!weapon.canAttack(entity, stack, action)) {
                return false;
            }

            coolDownTick = weapon.getAttackCoolDown(stack, action);
            lastMaxTick = coolDownTick;

            if (!entity.level().isClientSide()) {
                // 服务端，准备进行攻击
                this.preparingAttack = true;
                // 服务器宽限1tick以平衡延迟
                this.coolDownTick = Math.max(0, coolDownTick - 1);
            } else {
                // 客户端，通知服务端进入cd
                NetworkHandler.CHANNEL.sendToServer(new CPrepareMeleeAttack(action, origin, direction));

                int delay = weapon.getAttackDelay(entity, stack, action);
                var attack = new DelayAttack(delay, stack, action);
                if (attack.getDelay() == 0) {
                    attack.perform(entity);
                } else {
                    this.delayedActions.add(attack);
                }

                var moveInfo = weapon.getAttackMovement(entity, stack, action);
                if (moveInfo != null) {
                    var move = new DelayMove(moveInfo.getDelay(), moveInfo.getSpeed(), stack);
                    if (move.getDelay() == 0) {
                        move.perform(entity);
                    } else {
                        this.delayedActions.add(move);
                    }
                }

                LrTacticalAPI.getMeleeDisplay(stack).ifPresent(display -> {
                    if (display.getSounds().containsKey(action.getId())) {
                        entity.level().playLocalSound(
                                origin.x, origin.y, origin.z,
                                SoundEvent.createVariableRangeEvent(display.getSounds().get(action.getId())),
                                SoundSource.PLAYERS, 1.0F, 1.0F, false
                        );
                    }
                });
            }
            return true;
        }
        return false;
    }

    public void postAttack(MeleeAction action, List<Entity> entities) {
        ItemStack stack = entity.getMainHandItem();
        if (!this.preparingAttack) {
            return;
        }
        if (stack.getItem() instanceof IMeleeWeapon weapon) {
            weapon.attack(entity, stack, action, entities);
        }
        preparingAttack = false;
    }

    public static class DelayMove extends DelayTask {
        private final ItemStack stack;
        private final double speed;

        DelayMove(int delay, double speed, ItemStack stack) {
            super(delay);
            this.stack = stack;
            this.speed = speed;
        }

        @Override
        public void perform(Player player) {
            if (stack.getItem() instanceof IMeleeWeapon weapon && weapon.isSame(stack, player.getMainHandItem())) {
                double factor = player.onGround() ? 1.0 : 0.5;
                Vec3 motion = player.getLookAngle().multiply(1, 0, 1).normalize().scale(factor * speed);
                player.addDeltaMovement(motion);
            }
        }
    }

    public static class DelayAttack extends DelayTask {
        private final ItemStack stack;
        private final MeleeAction action;

        DelayAttack(int delay, ItemStack stack, MeleeAction action) {
            super(delay);
            this.action = action;
            this.stack = stack;
        }

        @Override
        public void perform(Player player) {
            if (stack.getItem() instanceof IMeleeWeapon weapon && weapon.isSame(stack, player.getMainHandItem())) {
                List<Entity> entities = weapon.collectTargets(player, stack, action, player.getEyePosition(), player.getLookAngle());
                NetworkHandler.CHANNEL.sendToServer(new CMeleeAttackRequest(action, entities));
            }
        }
    }

    public abstract static class DelayTask {
        protected int delay;

        protected DelayTask(int delay) {
            this.delay = delay;
        }

        abstract void perform(Player player);

        public boolean tick() {
            delay--;
            return delay <= 0;
        }

        public boolean expired() {
            return delay <= 0;
        }

        public int getDelay() {
            return delay;
        }
    }
}
