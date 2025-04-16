package me.xjqsh.lrtactical.resource;

import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface ICommonResourceProvider {
    ThrowableIndex<?, ?> getThrowableIndex(ResourceLocation id);

    Collection<ThrowableIndex<?, ?>> getThrowableIndexes();

    MeleeWeaponIndex<?> getMeleeIndex(ResourceLocation id);

    Collection<MeleeWeaponIndex<?>> getMeleeIndexes();
}
