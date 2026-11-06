package me.xjqsh.lrtactical.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.client.gui.components.CreativePackFilters;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.item.ICustomItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CreativePackFilters.class, remap = false)
public abstract class CreativePackFiltersMixin {
    @ModifyExpressionValue(method = "updateTab", at = @At(value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean lrt$includeCreativeTabs(boolean supported, CreativeModeTab tab, boolean force) {
        ResourceLocation tabId = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
        return supported || tabId != null && EquipmentMod.MOD_ID.equals(tabId.getNamespace());
    }

    @Inject(method = "getContentId", at = @At("HEAD"), cancellable = true)
    private static void lrt$getContentId(ItemStack stack, CallbackInfoReturnable<ResourceLocation> cir) {
        // Content packs share registered items; group them by their custom IDs.
        if (stack.getItem() instanceof ICustomItem item) {
            ResourceLocation id = item.getId(stack);
            if (id != null) {
                cir.setReturnValue(id);
            }
        }
    }
}
