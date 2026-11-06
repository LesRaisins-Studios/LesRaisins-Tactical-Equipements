package me.xjqsh.lrtactical.item.melee;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AttributeData {
    private final List<AttributeInfo> attributes = new ArrayList<>();

    public List<AttributeInfo> getAttributes() {
        return attributes;
    }

    public record AttributeInfo(
            @SerializedName("id")
            ResourceLocation id,

            @SerializedName("amount")
            float amount,

            @SerializedName("operation")
            AttributeModifier.Operation operation
    ) {}

    public static class Deserializer implements JsonDeserializer<AttributeData> {
        @Override
        public AttributeData deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Expected a JsonObject, get " + element);
            }
            JsonObject jsonObject = (JsonObject) element;
            AttributeData attributeData = new AttributeData();

            for (var entry : jsonObject.asMap().entrySet()) {
                ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                if (id == null) {
                    continue;
                }
                if (entry.getValue().isJsonPrimitive()) {
                    JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
                    if (primitive.isNumber()) {
                        float amount = primitive.getAsFloat();
                        attributeData.attributes.add(new AttributeInfo(id, amount, AttributeModifier.Operation.ADD_VALUE));
                    }
                } else if (entry.getValue().isJsonObject()) {
                    JsonObject attributeObject = entry.getValue().getAsJsonObject();
                    float amount = GsonHelper.getAsFloat(attributeObject, "amount", 0);
                    String operationId = GsonHelper.getAsString(attributeObject, "operation", "addition");

                    var operation = switch (operationId) {
                        case "addition" -> AttributeModifier.Operation.ADD_VALUE;
                        case "percent" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                        case "multiply" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                        default -> AttributeModifier.Operation.ADD_VALUE;
                    };

                    attributeData.attributes.add(new AttributeInfo(id, amount, operation));
                }
            }

            return attributeData;
        }
    }
}
