package me.xjqsh.lrtactical.item.melee;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import me.xjqsh.lrtactical.api.collision.ConeFilter;
import me.xjqsh.lrtactical.api.collision.ITargetFilter;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class CombatData {
    public static ITargetFilter DEFAULT_HITBOX = new ConeFilter(3.5, 60);

    public EnumMap<MeleeAction, List<MeleeAttackInfo>> attackInfo = new EnumMap<>(MeleeAction.class);

    @Nullable
    public MeleeAttackInfo getAttackInfo(MeleeAction action) {
        return getAttackInfo(action, 0);
    }

    @Nullable
    public MeleeAttackInfo getAttackInfo(MeleeAction action, int index) {
        List<MeleeAttackInfo> attackInfos = attackInfo.get(action);
        if (attackInfos != null && index < attackInfos.size()) {
            return attackInfos.get(index);
        }
        return null;
    }

    public static class MeleeMovement {
        @SerializedName("delay")
        private int delay = 0;

        @SerializedName("speed")
        private float speed = 0.5f;

        public int getDelay() {
            return delay;
        }

        public float getSpeed() {
            return speed;
        }
    }

    public static class MeleeAttackInfo {
        @SerializedName("factor")
        private float factor = 1.0f;

        @SerializedName("knockback")
        private float knockback = 0.02f;

        @SerializedName("cooldown")
        private int cooldown = 20;

        @SerializedName("delay")
        private int delay = 0;

        @SerializedName("hitbox")
        private ITargetFilter hitbox = DEFAULT_HITBOX;

        @SerializedName("durability_damage")
        private int durabilityDamage = 1;

        @SerializedName("movement")
        private MeleeMovement movement = null;

        public float getFactor() {
            return factor;
        }

        public float getKnockback() {
            return knockback;
        }

        public int getCooldown() {
            return cooldown;
        }

        public int getDelay() {
            return delay;
        }

        public ITargetFilter getHitbox() {
            return hitbox;
        }

        public int getDurabilityDamage() {
            return durabilityDamage;
        }

        @Nullable
        public MeleeMovement getMovement() {
            return movement;
        }
    }

    public static class Deserializer implements JsonDeserializer<CombatData> {
        @Override
        public CombatData deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Expected a JsonObject, get " + element);
            }
            JsonObject jsonObject = (JsonObject) element;
            CombatData combatData = new CombatData();

            for (MeleeAction action : MeleeAction.values()) {
                List<MeleeAttackInfo> attackInfos = new ArrayList<>();

                if (jsonObject.has(action.getId())) {
                    JsonElement actionElement = jsonObject.get(action.getId());
                    parseAttackInfo(ctx, action, actionElement, attackInfos);
                }

                if (!attackInfos.isEmpty()) {
                    combatData.attackInfo.put(action, attackInfos);
                }
            }
            return combatData;
        }

        private void parseAttackInfo(JsonDeserializationContext ctx, MeleeAction action, JsonElement actionElement,
                                     List<MeleeAttackInfo> attackInfos) throws JsonParseException {
            if (actionElement.isJsonArray()) {
                JsonArray actionArray = actionElement.getAsJsonArray();
                for (JsonElement ele : actionArray) {
                    MeleeAttackInfo attk = ctx.deserialize(ele, MeleeAttackInfo.class);
                    if (attk != null) {
                        attackInfos.add(attk);
                    }
                }
            } else if (actionElement.isJsonObject()) {
                MeleeAttackInfo attk = ctx.deserialize(actionElement, MeleeAttackInfo.class);
                if (attk != null) {
                    attackInfos.add(attk);
                }
            } else {
                throw new JsonParseException("Expected a JsonArray or JsonObject for action " + action.getId() + ", get " + actionElement);
            }
        }

    }
}
