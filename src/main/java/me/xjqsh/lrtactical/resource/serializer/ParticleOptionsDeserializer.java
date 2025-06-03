package me.xjqsh.lrtactical.resource.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;

import java.lang.reflect.Type;

public class ParticleOptionsDeserializer implements JsonDeserializer<ParticleOptions> {
    @Override
    public ParticleOptions deserialize(JsonElement ele, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        if (ele.isJsonPrimitive() && ele.getAsJsonPrimitive().isString()) {
            String particle = ele.getAsString();
            try {
                return ParticleArgument.readParticle(new StringReader(particle), BuiltInRegistries.PARTICLE_TYPE.asLookup());
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Can't parse particle type: " + particle, e);
            }
        } else {
            throw new JsonParseException("Expected a string for particle type, but found: " + ele);
        }
    }
}
