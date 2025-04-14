package me.xjqsh.lrtactical.api.melee;

public enum MeleeAction {
    LEFT("attack_left"),
    RIGHT("attack_right"),
    ;
    public final String animationSignal;

    MeleeAction(String signal) {
        this.animationSignal = signal;
    }

    public String getAnimation() {
        return animationSignal;
    }
}
