package me.jakemoritz.animebuzz.api.kitsu.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;


public class KitsuFilterDeserializer implements JsonDeserializer<String> {

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String kitsuMappingId = "";

        final JsonObject mappingsObject = json.getAsJsonObject();
        JsonArray dataArray = mappingsObject.getAsJsonArray("data");
        JsonObject dataObject = dataArray.get(0).getAsJsonObject();

        JsonPrimitive kitsuIdPrimitive;

        try {
            kitsuIdPrimitive = dataObject.getAsJsonPrimitive("id");
            kitsuMappingId = kitsuIdPrimitive.getAsString();
        } catch (ClassCastException e){

        }

        return kitsuMappingId;
    }
}
