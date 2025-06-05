package me.xjqsh.lrtactical.api.melee;

public enum AttackResult{
    HIT(true, false),
    CRIT(true, true),
    MISS(false, false)
    ;
    final boolean hit;
    final boolean crit;

    AttackResult(boolean hit, boolean crit) {
        this.hit = hit;
        this.crit = crit;
    }

    public boolean hit() {
        return hit;
    }

    public boolean crit() {
        return crit;
    }
}
