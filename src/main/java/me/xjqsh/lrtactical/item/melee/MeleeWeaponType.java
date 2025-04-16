package me.xjqsh.lrtactical.item.melee;

import com.google.gson.JsonElement;

public record MeleeWeaponType<T extends MeleeWeaponData>(
        MeleeWeaponType.MeleeDataSerializer<T> serializer) {

    @FunctionalInterface
    public interface MeleeDataSerializer<T extends MeleeWeaponData> {
        T parse(JsonElement json);
    }
}
