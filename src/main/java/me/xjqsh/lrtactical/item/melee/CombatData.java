package me.xjqsh.lrtactical.item.melee;

import com.google.gson.*;
import me.xjqsh.lrtactical.api.melee.MeleeAction;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.List;

public class CombatData {
    public EnumMap<MeleeAction, List<MeleeAttackInfo>> attackInfo = new EnumMap<>(MeleeAction.class);

    public static class Deserializer implements JsonDeserializer<CombatData> {
        @Override
        public CombatData deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Expected a JsonObject, get " + element);
            }
            JsonObject jsonObject = (JsonObject) element;
            for (MeleeAction action : MeleeAction.values()) {
                if (jsonObject.has(action.getId())) {
                    JsonElement actionElement = jsonObject.get(action.name());
                    if (actionElement.isJsonArray()) {
                        List<MeleeAttackInfo> attackInfos = ctx.deserialize(actionElement, List.class);
                        if (attackInfos != null) {
                            CombatData combatData = new CombatData();
                            combatData.attackInfo.put(action, attackInfos);
                            return combatData;
                        }
                    } else {
                        throw new JsonParseException("Expected a JsonArray for " + action.name() + ", get " + actionElement);
                    }
                }
            }
            return null;
        }
    }
}
