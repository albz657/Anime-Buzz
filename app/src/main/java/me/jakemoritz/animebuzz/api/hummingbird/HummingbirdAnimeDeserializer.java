package me.jakemoritz.animebuzz.api.hummingbird;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;


class HummingbirdAnimeDeserializer implements JsonDeserializer<HummingbirdAnimeHolder> {

    @Override
    public HummingbirdAnimeHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String englishTitle = "";
        String finishedAiringDate = "";
        String startedAiringDate = "";
        String imageURL = "";
        String showType = "";
        int episodeCount = 0;

        final JsonObject responseObject = json.getAsJsonObject();
        JsonObject animeObject = responseObject.getAsJsonObject("anime");

        JsonObject titlesObject = animeObject.getAsJsonObject("titles");
        JsonPrimitive englishPrimitive = null;
        JsonPrimitive posterImagePrimitive = null;
        JsonPrimitive finishedAiringDatePrimitive = null;
        JsonPrimitive startedAiringDatePrimitive = null;
        JsonPrimitive showTypePrimitive = null;
        JsonPrimitive episodeCountPrimitive = null;

        try {
            finishedAiringDatePrimitive = animeObject.getAsJsonPrimitive("finished_airing_date");
            finishedAiringDate = finishedAiringDatePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            episodeCountPrimitive = animeObject.getAsJsonPrimitive("episode_count");
            episodeCount = episodeCountPrimitive.getAsInt();
        } catch (ClassCastException e){

        }

        try {
            startedAiringDatePrimitive = animeObject.getAsJsonPrimitive("started_airing_date");
            startedAiringDate = startedAiringDatePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            showTypePrimitive = animeObject.getAsJsonPrimitive("show_type");
            showType = showTypePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            englishPrimitive = titlesObject.getAsJsonPrimitive("english");

            englishTitle = englishPrimitive.getAsString();

/*            Pattern englishPattern = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
            Matcher englishMatcher = englishPattern.matcher(englishPrimitive.getAsString());
            if (englishMatcher.find()){
                englishTitle = englishMatcher.group();
            }*/
        } catch (ClassCastException e){

        }

        try {
            posterImagePrimitive = animeObject.getAsJsonPrimitive("poster_image");
            imageURL = posterImagePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        return new HummingbirdAnimeHolder(englishTitle, imageURL, finishedAiringDate, startedAiringDate, showType, episodeCount);
    }/**/
}
