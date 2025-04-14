package me.xjqsh.lrtactical.api.collision;

import com.google.gson.annotations.SerializedName;
import me.xjqsh.lrtactical.util.VectorUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConeFilter implements ITargetFilter {
    @SerializedName("max_range")
    private double maxRange = 2.5d;

    @SerializedName("max_angle")
    private double maxAngle = 90d;

    @SerializedName("exclude_self")
    private boolean excludeSelf = true;

    public ConeFilter(double maxDistance, double maxAngle) {
        this.maxRange = maxDistance;
        this.maxAngle = maxAngle;
    }

    public ConeFilter(double maxDistance, double maxAngle, boolean excludeSelf) {
        this.maxRange = maxDistance;
        this.maxAngle = maxAngle;
        this.excludeSelf = excludeSelf;
    }

    @Override
    public @NotNull List<Entity> filterTargets(LivingEntity attacker, Vec3 origin, Vec3 direction) {
        List<Entity> targets = new ArrayList<>();
        AABB area = attacker.getBoundingBox().inflate(maxRange * 2, 0.25D, maxRange * 2);
        double halfAngle = maxAngle / 2.0;

        for(Entity livingentity : attacker.level().getEntitiesOfClass(Entity.class, area)) {
            boolean flag = !(livingentity instanceof ArmorStand armorStand) || !armorStand.isMarker();
            boolean inAngle = VectorUtil.isInAngle(origin, direction, livingentity, halfAngle, maxRange);

            boolean self = this.excludeSelf && livingentity == attacker;
            boolean allies = attacker.isAlliedTo(livingentity);

            if (!self && !allies && flag && inAngle) {
                targets.add(livingentity);
            }
        }
        return targets;
    }
}
