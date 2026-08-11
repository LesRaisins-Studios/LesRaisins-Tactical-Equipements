package me.xjqsh.lrtactical.resource.serializer;

import com.google.gson.*;
import org.joml.Vector3f;

import java.lang.reflect.Type;

/**
 * 将形如 [x, y, z] 的 JSON 数组反序列化为 {@link Vector3f}。
 * 用于 display 资源中与 transforms 同级的 display_offset 字段。
 */
public class Vector3fDeserializer implements JsonDeserializer<Vector3f> {
    @Override
    public Vector3f deserialize(JsonElement ele, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        if (!ele.isJsonArray()) {
            throw new JsonParseException("Expected an array for Vector3f, but found: " + ele);
        }
        JsonArray array = ele.getAsJsonArray();
        if (array.size() != 3) {
            throw new JsonParseException("Expected an array of 3 elements for Vector3f, but found: " + ele);
        }
        return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }
}
