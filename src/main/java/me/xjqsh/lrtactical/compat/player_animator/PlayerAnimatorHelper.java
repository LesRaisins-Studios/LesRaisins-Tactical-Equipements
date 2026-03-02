package me.xjqsh.lrtactical.compat.player_animator;

/**
 * Helper class for initializing Player Animator integration
 * Separated to avoid classloading issues when mod is not present
 */
public class PlayerAnimatorHelper {
    public static void initIntegration() {
        PlayerAnimatorIntegration.init();
    }
}
