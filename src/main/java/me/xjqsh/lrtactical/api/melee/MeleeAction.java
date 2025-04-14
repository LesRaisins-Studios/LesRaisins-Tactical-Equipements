package me.xjqsh.lrtactical.api.melee;

public enum MeleeAction {
    LEFT("attack_left"),
    RIGHT("attack_right"),
    ;
    public final String id;

    MeleeAction(String signal) {
        this.id = signal;
    }

    public String getId() {
        return id;
    }
}
