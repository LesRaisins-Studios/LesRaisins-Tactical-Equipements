package me.xjqsh.lrtactical.item.index;

import me.xjqsh.lrtactical.item.melee.MeleeData;

public class MeleeIndex<T extends MeleeData> {
    private final T data;

    public MeleeIndex(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
