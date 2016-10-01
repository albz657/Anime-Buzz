package me.jakemoritz.animebuzz.api.hummingbird;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AnimeDeserializer implements JsonDeserializer<HummingbirdAnimeHolder> {

    @Override
    public HummingbirdAnimeHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String englishTitle = "";
        String finishedAiringDate = "";
        String imageURL = "";

        final JsonObject responseObject = json.getAsJsonObject();
        JsonObject animeObject = responseObject.getAsJsonObject("anime");

        JsonObject titlesObject = animeObject.getAsJsonObject("titles");
        JsonPrimitive englishPrimitive = null;
        JsonPrimitive posterImagePrimitive = null;
        JsonPrimitive finishedAiringDatePrimitive = null;

        try {
            finishedAiringDatePrimitive = animeObject.getAsJsonPrimitive("finished_airing_date");
            finishedAiringDate = finishedAiringDatePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            englishPrimitive = titlesObject.getAsJsonPrimitive("english");

            Pattern englishPattern = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
            Matcher englishMatcher = englishPattern.matcher(englishPrimitive.getAsString());
            if (englishMatcher.find()){
                englishTitle = englishMatcher.group();
            }
        } catch (ClassCastException e){

        }

        try {
            posterImagePrimitive = animeObject.getAsJsonPrimitive("poster_image");
            imageURL = posterImagePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        return new HummingbirdAnimeHolder(englishTitle, imageURL, finishedAiringDate);
    }
}
