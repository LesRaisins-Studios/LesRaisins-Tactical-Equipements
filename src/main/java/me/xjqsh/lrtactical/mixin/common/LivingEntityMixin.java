package me.xjqsh.lrtactical.mixin.common;

import com.tacz.guns.init.ModDamageTypes;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.capability.CombatPropertiesProvider;
import me.xjqsh.lrtactical.capability.CustomItemCoolDownsProvider;
import me.xjqsh.lrtactical.item.FlashShieldItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ICapabilityProvider {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow public abstract ItemStack getMainHandItem();

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    public void isDamageSourceBlocked(DamageSource pDamageSource, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getMainHandItem();
        if (stack.getItem() instanceof FlashShieldItem && pDamageSource.is(ModDamageTypes.BULLETS_TAG)) {
            boolean isDrawing = this.getCapability(CombatPropertiesProvider.CAPABILITY)
                    .map(CombatProperties::isDrawing)
                    .orElse(false);
            boolean isDisabled = this.getCapability(CustomItemCoolDownsProvider.CAPABILITY)
                    .map(cap -> cap.isOnCooldown(new ResourceLocation("shield_disabled")))
                    .orElse(false);
            boolean isBlocking = !isDrawing && !isDisabled && stack.getDamageValue() < stack.getMaxDamage();

            if (isBlocking) {
                Vec3 vec32 = pDamageSource.getSourcePosition();
                if (vec32 != null) {
                    Vec3 vec3 = this.getViewVector(1.0F);
                    Vec3 vec31 = vec32.vectorTo(this.position()).normalize();
                    vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
                    if (vec31.dot(vec3) < 0.0D) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    public void isBlocking(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getMainHandItem();
        if (stack.getItem() instanceof FlashShieldItem) {
            boolean isDrawing = this.getCapability(CombatPropertiesProvider.CAPABILITY)
                    .map(CombatProperties::isDrawing)
                    .orElse(false);
            boolean isDisabled = this.getCapability(CustomItemCoolDownsProvider.CAPABILITY)
                    .map(cap -> cap.isOnCooldown(new ResourceLocation("shield_disabled")))
                    .orElse(false);
            cir.setReturnValue(!isDrawing && !isDisabled && stack.getDamageValue() < stack.getMaxDamage());
        }
    }
}
