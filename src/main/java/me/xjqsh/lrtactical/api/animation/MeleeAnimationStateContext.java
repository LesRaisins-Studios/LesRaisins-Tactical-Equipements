package me.xjqsh.lrtactical.api.animation;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;

public class MeleeAnimationStateContext extends BaseAnimationStateContext {
    /**
     * 某个攻击动作已进行的次数
     * @param action 攻击动作
     * @return 次数；如果传入错误，返回0
     */
    public int getActionCount(String action) {
        MeleeAction action1 = switch (action) {
            case "attack_left" -> MeleeAction.LEFT;
            case "attack_right" -> MeleeAction.RIGHT;
            default -> null;
        };
        if (action1 == null) {
            return 0;
        }
        return this.processCameraEntity(entity -> entity.getCapability(CombatPropertiesProvider.CAPABILITY)
                .map(cap -> cap.getActionCount(action1))
                .orElse(0)).orElse(0);
    }
}
