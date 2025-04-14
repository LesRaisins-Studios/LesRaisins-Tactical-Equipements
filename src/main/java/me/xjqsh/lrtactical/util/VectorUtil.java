package me.xjqsh.lrtactical.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VectorUtil {

    /**
     * 计算点到某AABB的最短路径
     * @param point 起点
     * @param box AABB
     */
    public static Vec3 distanceVector(Vec3 point, AABB box) {
        double dx = 0;
        if (box.minX > point.x) {
            dx = box.minX - point.x;
        } else if (box.maxX < point.x) {
            dx = box.maxX - point.x;
        }
        double dy = 0;
        if (box.minY > point.y) {
            dy = box.minY - point.y;
        } else if (box.maxY < point.y) {
            dy = box.maxY - point.y;
        }
        double dz = 0;
        if (box.minZ > point.z) {
            dz = box.minZ - point.z;
        } else if (box.maxZ < point.z) {
            dz = box.maxZ - point.z;
        }
        return new Vec3(dx, dy, dz);
    }

    /**
     * 计算两个向量之间的夹角
     */
    public static double angleBetween(Vec3 v1, Vec3 v2) {
        return Math.toDegrees(Math.acos(v1.normalize().dot(v2.normalize())));
    }

    public static boolean isInAngle(Vec3 origin, Vec3 view, Entity target, double maxAngle, double maxDistance) {
        Vec3 positionVector = target.position().add(0, target.getBbHeight() / 2F, 0).subtract(origin);

        Vec3 distanceVector = distanceVector(origin, target.getBoundingBox());
        double distance = distanceVector.length();

        if (distance == 0) return true;

        if (distance > maxDistance) {
            return false;
        }

        // 满足最短路或者实体中心在最大夹角内任一条件即可
        return angleBetween(view, distanceVector) <= maxAngle || angleBetween(view, positionVector) <= maxAngle;
    }
}
