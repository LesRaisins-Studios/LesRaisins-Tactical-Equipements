package me.xjqsh.lrtactical.mixin.common;

import me.xjqsh.lrtactical.capability.CustomItemCoolDownsProvider;
import me.xjqsh.lrtactical.item.FlashShieldItem;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SShieldDisable;
import me.xjqsh.lrtactical.network.message.SShieldShake;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract ItemCooldowns getCooldowns();

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
    public void disableShield(boolean pBecauseOfAxe, CallbackInfo ci) {
        ItemStack stack = this.getMainHandItem();
        if (stack.getItem() instanceof FlashShieldItem item) {
            this.getCooldowns().addCooldown(item, 30);
            this.getCapability(CustomItemCoolDownsProvider.CAPABILITY).ifPresent(cap ->{
                cap.addCooldown(new ResourceLocation("shield_disabled"), 30);
            });
            if ((Object)this instanceof ServerPlayer serverPlayer) {
                // 发送消息到客户端，触发动画
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SShieldDisable()
                );
            }
            ci.cancel();
        }
    }
}
