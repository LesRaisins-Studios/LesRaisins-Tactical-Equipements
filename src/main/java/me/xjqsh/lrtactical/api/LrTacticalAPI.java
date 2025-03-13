package me.xjqsh.lrtactical.api;

import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.client.resource.LrClientAssetsManager;
import me.xjqsh.lrtactical.client.resource.display.ThrowableDisplayInstance;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.Optional;

public class LrTacticalAPI {
    @OnlyIn(Dist.CLIENT)
    public static Optional<ThrowableDisplayInstance> getThrowableDisplay(ItemStack stack) {
        if (!(stack.getItem() instanceof IThrowable item)) {
            return Optional.empty();
        }
        return Optional.ofNullable(LrClientAssetsManager.INSTANCE.getThrowableDisplay(item.getId(stack)));
    }

    public static Optional<ThrowableIndex<?, ?>> getThrowableIndex(ItemStack stack) {
        if (!(stack.getItem() instanceof IThrowable item)) {
            return Optional.empty();
        }
        return Optional.ofNullable(CommonAssetsManager.get().getThrowableIndex(item.getId(stack)));
    }

    public static Collection<ThrowableIndex<?, ?>> getThrowableIndexes() {
        return CommonAssetsManager.get().getThrowableIndexes();
    }
}
