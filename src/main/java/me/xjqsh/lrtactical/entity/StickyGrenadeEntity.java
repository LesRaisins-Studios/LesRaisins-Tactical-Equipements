package me.xjqsh.lrtactical.entity;

import me.xjqsh.lrtactical.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

public class StickyGrenadeEntity extends GrenadeEntity {
    public static EntityType<StickyGrenadeEntity> TYPE = EntityType.Builder.<StickyGrenadeEntity>of(StickyGrenadeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .setCustomClientFactory(StickyGrenadeEntity::new)
            .sized(0.3f, 0.3f)
            .noSave()
            .noSummon()
            .fireImmune()
            .build("sticky_grenade_entity");

    private static final EntityDataAccessor<Boolean> STICKED = SynchedEntityData.defineId(StickyGrenadeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STUCK_ENTITY_ID = SynchedEntityData.defineId(StickyGrenadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Rotations> STUCK_OFFSET = SynchedEntityData.defineId(StickyGrenadeEntity.class, EntityDataSerializers.ROTATIONS);
    private static final EntityDataAccessor<Rotations> STUCK_ROTATION = SynchedEntityData.defineId(StickyGrenadeEntity.class, EntityDataSerializers.ROTATIONS);

    @Nullable
    private BlockPos stuckBlockPos;
    @Nullable
    private UUID stuckEntityUUID;

    public StickyGrenadeEntity(LivingEntity entity, Level level, int lifeTime) {
        super(TYPE, entity, level, lifeTime);
    }

    public StickyGrenadeEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        super(TYPE, level);
    }

    public StickyGrenadeEntity(EntityType<StickyGrenadeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STICKED, false);
        this.entityData.define(STUCK_ENTITY_ID, -1);
        this.entityData.define(STUCK_OFFSET, new Rotations(0, 0, 0));
        this.entityData.define(STUCK_ROTATION, new Rotations(0, 0, 0));
    }

    @Override
    public boolean shouldBounce() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.entityData.get(STICKED)) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);

            // Handle Block Stuck Logic (Server Side Only)
            if (!this.level().isClientSide && this.stuckBlockPos != null) {
                BlockState state = this.level().getBlockState(this.stuckBlockPos);
                if (state.isAir()) {
                    this.detach();
                }
            }

            // Handle Entity Stuck Logic (Both Sides)
            int entityId = this.entityData.get(STUCK_ENTITY_ID);
            if (entityId != -1) {
                Entity entity = this.level().getEntity(entityId);
                // On server side, fallback to UUID lookup if ID lookup fails (e.g. after load)
                if (entity == null && !this.level().isClientSide && this.stuckEntityUUID != null && this.level() instanceof ServerLevel serverLevel) {
                    entity = serverLevel.getEntity(this.stuckEntityUUID);
                    if (entity != null) {
                        // Restore ID
                        this.entityData.set(STUCK_ENTITY_ID, entity.getId());
                    }
                }

                if (entity != null && entity.isAlive()) {
                    Rotations offsetRot = this.entityData.get(STUCK_OFFSET);
                    Vec3 offset = new Vec3(offsetRot.getX(), offsetRot.getY(), offsetRot.getZ());
                    
                    // Use lerped rotation for smoother client rendering
                    float yRot = this.getEntityRotation(entity);
                    
                    Vec3 rotatedOffset = offset.yRot(-yRot * Mth.DEG_TO_RAD);
                    Vec3 newPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0).add(rotatedOffset);
                    this.setPos(newPos);
                    
                    Rotations relativeRot = this.entityData.get(STUCK_ROTATION);
                    this.setYRot(yRot + relativeRot.getY());
                    this.setXRot(relativeRot.getX());
                } else if (!this.level().isClientSide) {
                    // Entity lost or dead
                    this.detach();
                }
            }
        }
    }

    @Override
    protected void updateRotation() {
        if (this.entityData.get(STICKED)) {
            return;
        }
        super.updateRotation();
    }

    private void alignToVec(Vec3 vec) {
        if (vec.lengthSqr() < 1.0E-7D) return;
        Vec3 normalized = vec.normalize();
        this.setYRot((float)(Mth.atan2(-normalized.x, normalized.z) * (double)(180F / (float)Math.PI)));
        this.setXRot((float)(-Mth.atan2(normalized.y, Math.sqrt(normalized.x * normalized.x + normalized.z * normalized.z)) * (double)(180F / (float)Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
    
    @Override
    public BounceResult doMultiBounce(Vec3 deltaMovement) {
        if (this.entityData.get(STICKED)) {
            return new BounceResult(this.position(), Vec3.ZERO);
        }

        Vec3 start = this.position();
        Vec3 end = start.add(deltaMovement);
        HitResult hitResult = this.getHitResult(start, end, deltaMovement, this::canHitEntity, this.level());

        if (hitResult.getType() != HitResult.Type.MISS) {
            if (!this.level().isClientSide()) {
                this.onHit(hitResult);
            }
            return new BounceResult(this.position(), Vec3.ZERO);
        }

        return new BounceResult(end, deltaMovement);
    }

    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pPosRotationIncrements, boolean pTeleport) {
        if (this.entityData.get(STICKED) && this.entityData.get(STUCK_ENTITY_ID) != -1) {
            // Ignore server position updates when stuck to an entity to prevent jitter
            // We still accept rotation updates if needed, or completely ignore
            return;
        }
        super.lerpTo(pX, pY, pZ, pYRot, pXRot, pPosRotationIncrements, pTeleport);
    }

    private void detach() {
        this.entityData.set(STICKED, false);
        this.entityData.set(STUCK_ENTITY_ID, -1);
        this.setNoGravity(false);
        this.stuckBlockPos = null;
        this.stuckEntityUUID = null;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void onHit(HitResult result) {
        if (this.entityData.get(STICKED)) return;
        if (result.getType() == HitResult.Type.MISS) return;

        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) result;
            BlockPos resultPos = blockResult.getBlockPos();
            BlockState state = this.level().getBlockState(resultPos);
            SoundEvent event = state.getBlock().getSoundType(state, this.level(), resultPos, this).getStepSound();
            
            this.level().playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z,
                    event, SoundSource.AMBIENT, 2.0F, 1.0F);
            this.level().playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z,
                    ModSounds.GRENADE_BOUNCE.get(), SoundSource.AMBIENT, 2.0F, 1.0F);
            
            this.entityData.set(STICKED, true);
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(result.getLocation().subtract(
                    -blockResult.getDirection().getStepX() * 0.15,
                    -blockResult.getDirection().getStepY() * 0.15 + 0.15,
                    -blockResult.getDirection().getStepZ() * 0.15
            ));

            
            this.stuckBlockPos = resultPos;
            
            Vec3 normal = Vec3.atLowerCornerOf(blockResult.getDirection().getNormal());
            this.alignToVec(normal);
        } else if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityResult = (EntityHitResult) result;
            Entity entity = entityResult.getEntity();
            if (entity == this.getOwner() || entity == this.getVehicle()) return;

            entity.hurt(entity.damageSources().thrown(this, this.getOwner()), this.getHitDamage());

            this.entityData.set(STICKED, true);
            this.setNoGravity(true);
            this.setDeltaMovement(result.getLocation().subtract(this.position()));
            
            // Record entity and offset
            this.stuckEntityUUID = entity.getUUID();
            this.entityData.set(STUCK_ENTITY_ID, entity.getId());
            
            // Calculate offset in entity's local coordinate system (relative to Y rotation)
            Vec3 globalOffset = result.getLocation().subtract(entity.position().add(0, entity.getBbHeight() * 0.5, 0));
            this.alignToVec(globalOffset);
            float entityYRot = this.getEntityRotation(entity);
            Vec3 localOffset = globalOffset.yRot(entityYRot * Mth.DEG_TO_RAD).scale(0.75f);
            
            this.entityData.set(STUCK_OFFSET, new Rotations((float)localOffset.x, (float)localOffset.y, (float)localOffset.z));
            
            float relativeYaw = this.getYRot() - entityYRot;
            float relativePitch = this.getXRot(); // Pitch relative to world (assume entity doesn't pitch usually)
            this.entityData.set(STUCK_ROTATION, new Rotations(relativePitch, relativeYaw, 0));
        }
    }

    private float getEntityRotation(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.yBodyRot;
        }
        return entity.getYRot();
    }
}
