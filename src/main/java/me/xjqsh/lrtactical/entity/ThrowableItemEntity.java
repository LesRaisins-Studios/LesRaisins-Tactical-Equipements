package me.xjqsh.lrtactical.entity;


import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.init.ModItems;
import me.xjqsh.lrtactical.init.ModSounds;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SThrowableSound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import net.minecraftforge.network.PacketDistributor;

public abstract class ThrowableItemEntity extends ThrowableItemProjectile implements IEntityAdditionalSpawnData {
    private int life = 100;
    private float gravity = 0.07f;
    private double bounceFactor = 0.75;
    private boolean shouldBounce = true;

    public ThrowableItemEntity(EntityType<? extends ThrowableItemEntity> type, LivingEntity entity, Level level, int lifeTime) {
        super(type, entity, level);
        this.life = lifeTime;
    }

    public ThrowableItemEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.THROWABLE.get();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() != HitResult.Type.MISS && !this.shouldBounce) {
            this.onDeath();
            return;
        }
        switch (result.getType()) {
            case BLOCK -> {
                BlockHitResult blockResult = (BlockHitResult) result;
                BlockPos resultPos = blockResult.getBlockPos();
                BlockState state = this.level().getBlockState(resultPos);
                SoundEvent event = state.getBlock().getSoundType(state, this.level(), resultPos, this).getStepSound();
                double speed = this.getDeltaMovement().length();
                if (speed > 0.1) {
                    this.level().playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z,
                            event, SoundSource.AMBIENT, 1.0F, 1.0F);
                    this.level().playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z,
                            ModSounds.GRENADE_BOUNCE.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
                }
                this.bounce(blockResult.getDirection());

                state.onProjectileHit(this.level(), state, blockResult, this);
            }
            case ENTITY -> {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity entity = entityResult.getEntity();
                if (entity == this.getOwner() || entity == this.getVehicle()) return;
                double speed_e = this.getDeltaMovement().length();
                if (speed_e > 0.1) {
                    entity.hurt(entity.damageSources().thrown(this, this.getOwner()), 1.0F);
                }
                this.bounce(Direction.getNearest(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()).getOpposite());
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.25, 1.0, 0.25));
            }
            default -> {}
        }
    }

    protected void bounce(Direction direction) {
        double factor = this.getBounceFactor();
        switch (direction.getAxis()) {
            case X:
                this.setDeltaMovement(this.getDeltaMovement().multiply(-factor/1.5, factor, factor));
                break;
            case Y:
                this.setDeltaMovement(this.getDeltaMovement().multiply(factor, -factor/3, factor));
                if (this.getDeltaMovement().y() < this.getGravity()) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0, 1));
                }
                break;
            case Z:
                this.setDeltaMovement(this.getDeltaMovement().multiply(factor, factor, -factor/1.5));
                break;
        }
    }

    public void playBounceSound() {

    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= life) {
            if (!this.level().isClientSide()) {
                this.onDeath();
            }
        }
    }

    public void onDeath() {
        this.discard();
        if (!this.level().isClientSide()) {
            playThrowableSound("death", 16.0F, 1.0F);
        }
    }

    public void playThrowableSound(String key, float volume, float pitch) {
        ItemStack stack = this.getItem();
        if (stack.getItem() instanceof IThrowable iThrowable) {
            ResourceLocation id = iThrowable.getId(stack);
            var packet = new SThrowableSound(id, key, this.position(), volume, pitch);
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(this.getX(), this.getY(), this.getZ(), 64, this.level().dimension())),
                    packet
            );
        }
    }

    @Override
    protected float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public double getBounceFactor() {
        return bounceFactor;
    }

    public void setBounceFactor(double bounceFactor) {
        this.bounceFactor = bounceFactor;
    }

    public boolean shouldBounce() {
        return shouldBounce;
    }

    public void setShouldBounce(boolean shouldBounce) {
        this.shouldBounce = shouldBounce;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(life);
        buffer.writeFloat(gravity);
        buffer.writeDouble(bounceFactor);
        buffer.writeBoolean(shouldBounce);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        life = additionalData.readInt();
        gravity = additionalData.readFloat();
        bounceFactor = additionalData.readDouble();
        shouldBounce = additionalData.readBoolean();
    }
}
