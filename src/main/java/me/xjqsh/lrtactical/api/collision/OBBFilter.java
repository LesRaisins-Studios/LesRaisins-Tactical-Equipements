package me.xjqsh.lrtactical.api.collision;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class OBBFilter implements ITargetFilter {
    @SerializedName("max_range")
    private double maxRange = 4d;

    @SerializedName("half_width")
    private double halfWidth = 0.5f;

    @SerializedName("half_height")
    private double halfHeight = 0.5f;

    @SerializedName("exclude_self")
    private boolean excludeSelf = true;

    @SerializedName("roll")
    private float roll = 0.0f;

    @NotNull
    @Override
    public List<Entity> filterTargets(LivingEntity attacker, Vec3 origin, Vec3 direction) {
        Quaternionf rotation = new Quaternionf().rotationYXZ(
                -attacker.getYRot() * Mth.DEG_TO_RAD,
                attacker.getXRot() * Mth.DEG_TO_RAD,
                roll * Mth.DEG_TO_RAD
        );
        Vector3f extents = new Vec3(halfWidth, halfHeight, maxRange / 2).toVector3f();
        Vector3f center = origin.add(direction.scale(maxRange / 2)).toVector3f();

        OBB obb = new OBB(center, extents, rotation);
        List<Entity> targets = new ArrayList<>();
        AABB area = attacker.getBoundingBox().inflate(maxRange * 2, maxRange, maxRange * 2);

        for(Entity livingentity : attacker.level().getEntitiesOfClass(Entity.class, area)) {
            boolean flag = !(livingentity instanceof ArmorStand armorStand) || !armorStand.isMarker();
            boolean see = ITargetFilter.hasLineOfSight(attacker, livingentity);
            boolean self = this.excludeSelf && livingentity == attacker;

            if (!self && flag && obb.interactsWithAABB(livingentity.getBoundingBox()) && see) {
                targets.add(livingentity);
            }
        }
        return targets;
    }

    @Override
    public double getMaxRange() {
        return maxRange;
    }
}
