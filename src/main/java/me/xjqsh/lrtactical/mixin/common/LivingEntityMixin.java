package me.xjqsh.lrtactical.mixin.common;

import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import me.xjqsh.lrtactical.item.ShieldItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ICapabilityProvider {

    @Shadow public abstract ItemStack getMainHandItem();

    @Inject(method = "isDamageSourceBlocked", at = @At("TAIL"))
    public void isDamageSourceBlocked(DamageSource pDamageSource, CallbackInfoReturnable<Boolean> cir) {

    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    public void isBlocking(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getMainHandItem();
        if (stack.getItem() instanceof ShieldItem) {
            boolean isDrawing = this.getCapability(CombatPropertiesProvider.CAPABILITY)
                    .map(CombatProperties::isDrawing)
                    .orElse(false);
            cir.setReturnValue(!isDrawing && stack.getDamageValue() < stack.getMaxDamage());
        }
    }
}
