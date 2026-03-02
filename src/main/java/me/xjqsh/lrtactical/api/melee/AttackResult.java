package me.xjqsh.lrtactical.api.melee;

public enum AttackResult{
    HIT(true, false, false),
    CRIT(true, true, false),
    KILL(true, false, true),
    CRIT_KILL(true, true, true),
    MISS(false, false, false)
    ;
    final boolean hit;
    final boolean crit;
    final boolean kill;

    AttackResult(boolean hit, boolean crit, boolean kill) {
        this.hit = hit;
        this.crit = crit;
        this.kill = kill;
    }

    public boolean hit() {
        return hit;
    }

    public boolean crit() {
        return crit;
    }

    public boolean kill() {
        return kill;
    }
}
