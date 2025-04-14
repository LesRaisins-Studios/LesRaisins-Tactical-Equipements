package me.xjqsh.lrtactical.api.collision;

import com.google.gson.*;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

@FunctionalInterface
public interface ITargetFilter {
    @NotNull List<Entity> filterTargets(LivingEntity attacker, Vec3 origin, Vec3 direction);

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
                default -> throw new JsonParseException("Unknown filter type: " + typeName);
            };
        }
    }
}
