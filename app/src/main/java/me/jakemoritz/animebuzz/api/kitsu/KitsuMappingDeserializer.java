package me.jakemoritz.animebuzz.api.kitsu;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class KitsuMappingDeserializer implements JsonDeserializer<String> {

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String kitsuMappingId = "";

        final JsonObject mappingsObject = json.getAsJsonObject();
        JsonObject dataObject = mappingsObject.getAsJsonObject("data");
        JsonElement data = dataObject.get("id");

        try {
            kitsuMappingId = data.getAsString();
        } catch (ClassCastException e){

        }

        return kitsuMappingId;
    }
}
