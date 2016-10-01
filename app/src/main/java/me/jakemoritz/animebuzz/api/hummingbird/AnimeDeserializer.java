package me.jakemoritz.animebuzz.api.hummingbird;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;


public class AnimeDeserializer implements JsonDeserializer<HummingbirdAnimeHolder> {

    @Override
    public HummingbirdAnimeHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
