package me.xjqsh.lrtactical.api.collision;

import com.google.gson.*;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

@FunctionalInterface
public interface ITargetFilter {
    @NotNull List<Entity> filterTargets(LivingEntity attacker, Vec3 origin, Vec3 direction);

    default double getMaxRange() {
        return 2.5d;
    }

    static boolean hasLineOfSight(LivingEntity attacker, Entity pEntity) {
        if (pEntity.level() == attacker.level()) {
            Vec3 vec3 = new Vec3(attacker.getX(), attacker.getEyeY(), attacker.getZ());
            if (clip(attacker, new Vec3(pEntity.getX(), pEntity.getEyeY(), pEntity.getZ()), vec3)) {
                return true;
            }

            AABB boundingBox = pEntity.getBoundingBox();
            for (double x : new double[]{boundingBox.minX, boundingBox.maxX}) {
                for (double y : new double[]{boundingBox.minY, boundingBox.maxY}) {
                    for (double z : new double[]{boundingBox.minZ, boundingBox.maxZ}) {
                        if (clip(attacker, new Vec3(x, y, z), vec3)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean clip(LivingEntity attacker, Vec3 vec31, Vec3 vec3) {
        if (vec31.distanceTo(vec3) > 128.0D) {
            return false;
        } else {
            return attacker.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, attacker))
                    .getType() == HitResult.Type.MISS;
        }
    }

    class Deserializer implements JsonDeserializer<ITargetFilter> {
        @Override
        public ITargetFilter deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Expected a JsonObject, get " + element);
            }
            JsonObject jsonObject = (JsonObject) element;

            String typeName = GsonHelper.getAsString(jsonObject, "type");
            return switch (typeName) {
                case "cone" ->  ctx.deserialize(jsonObject, ConeFilter.class);
                case "ray" ->  ctx.deserialize(jsonObject, RayFilter.class);
                case "obb" ->  ctx.deserialize(jsonObject, OBBFilter.class);
                default -> throw new JsonParseException("Unknown filter type: " + typeName);
            };
        }
    }
}
