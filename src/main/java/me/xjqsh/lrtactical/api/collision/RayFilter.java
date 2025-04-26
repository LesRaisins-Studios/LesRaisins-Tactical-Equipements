package me.xjqsh.lrtactical.api.collision;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.HitboxHelper;
import me.xjqsh.lrtactical.util.VectorUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RayFilter implements ITargetFilter {
    @SerializedName("max_range")
    private double maxRange = 2.5d;

    @SerializedName("penetration")
    private int penetration = 0;

    public RayFilter(double maxDistance, int penetration) {
        this.maxRange = maxDistance;
        this.penetration = penetration;
    }

    public RayFilter(double maxDistance, int penetration, boolean excludeSelf, boolean excludeAllies) {
        this.maxRange = maxDistance;
        this.penetration = penetration;
    }

    @Override
    public @NotNull List<Entity> filterTargets(LivingEntity attacker, Vec3 origin, Vec3 direction) {
        var to = origin.add(direction.normalize().scale(maxRange));
        List<RayEntityHitResult> targets = findEntitiesOnPath(attacker, origin, to);

        return targets.stream().limit(penetration + 1).map(RayEntityHitResult::getEntity).toList();
    }

    @NotNull
    public List<RayEntityHitResult> findEntitiesOnPath(LivingEntity attacker, Vec3 startVec, Vec3 endVec) {
        List<RayEntityHitResult> hitEntities = new ArrayList<>();
        AABB area = attacker.getBoundingBox().expandTowards(attacker.getViewVector(1.0f).scale(maxRange)).inflate(1.0);

        List<Entity> entities = attacker.level().getEntities(attacker, area, EntitySelector.NO_SPECTATORS);
        for (Entity entity : entities) {
            if (entity.equals(attacker) || entity.equals(attacker.getVehicle()) || !entity.isAlive()) {
                continue;
            }

            Optional<Vec3> optional = entity.getBoundingBox().clip(startVec, endVec);
            if (optional.isPresent()) {
                var result = new RayEntityHitResult(entity, optional.get(), startVec);
                hitEntities.add(result);
            } else if(entity.getBoundingBox().contains(startVec)) {
                var result = new RayEntityHitResult(entity, startVec, startVec);
                hitEntities.add(result);
            }
        }
        hitEntities.sort((a, b)-> (int)(a.getDistanceSqr() - b.getDistanceSqr()));
        return hitEntities;
    }

    public static class RayEntityHitResult extends EntityHitResult {
        private final Vec3 source;
        private final double distanceSqr;

        public RayEntityHitResult(Entity entity, Vec3 location, Vec3 source) {
            super(entity, location);
            this.source = source;
            this.distanceSqr = source.distanceToSqr(location);
        }

        public Vec3 getSource() {
            return source;
        }

        public double getDistanceSqr() {
            return distanceSqr;
        }
    }
}
