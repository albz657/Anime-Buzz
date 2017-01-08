package me.jakemoritz.animebuzz.api.kitsu;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;


class KitsuDeserializer implements JsonDeserializer<KitsuAnimeHolder> {

    @Override
    public KitsuAnimeHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String englishTitle = "";
        String finishedAiringDate = "";
        String startedAiringDate = "";
        String imageURL = "";
        String showType = "";
        int episodeCount = 0;
        String kitsuId = "";

        final JsonObject responseObject = json.getAsJsonObject();
        JsonObject dataObject = responseObject.getAsJsonObject("data");
        JsonObject attributesObject = dataObject.getAsJsonObject("attributes");
        JsonObject titlesObject = attributesObject.getAsJsonObject("titles");
        JsonObject posterObject = attributesObject.getAsJsonObject("posterImage");

        JsonPrimitive kitsuIdPrimitive = null;
        JsonPrimitive englishPrimitive = null;
        JsonPrimitive posterImagePrimitive = null;
        JsonPrimitive finishedAiringDatePrimitive = null;
        JsonPrimitive startedAiringDatePrimitive = null;
        JsonPrimitive showTypePrimitive = null;
        JsonPrimitive episodeCountPrimitive = null;

        try {
            kitsuIdPrimitive = dataObject.getAsJsonPrimitive("id");
            kitsuId = kitsuIdPrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            finishedAiringDatePrimitive = attributesObject.getAsJsonPrimitive("endDate");
            finishedAiringDate = finishedAiringDatePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            episodeCountPrimitive = attributesObject.getAsJsonPrimitive("episodeCount");
            if (episodeCountPrimitive != null){
                episodeCount = episodeCountPrimitive.getAsInt();
            }
        } catch (ClassCastException e){

        }

        try {
            startedAiringDatePrimitive = attributesObject.getAsJsonPrimitive("startDate");
            startedAiringDate = startedAiringDatePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            showTypePrimitive = attributesObject.getAsJsonPrimitive("showType");
            if (showTypePrimitive != null){
                showType = showTypePrimitive.getAsString();
            }
        } catch (ClassCastException e){

        }

        try {
            englishPrimitive = titlesObject.getAsJsonPrimitive("en");
            englishTitle = englishPrimitive.getAsString();
        } catch (ClassCastException e){

        }

        try {
            posterImagePrimitive = posterObject.getAsJsonPrimitive("original");
            imageURL = posterImagePrimitive.getAsString();
        } catch (ClassCastException e){

        }

        return new KitsuAnimeHolder(englishTitle, imageURL, finishedAiringDate, startedAiringDate, showType, episodeCount, kitsuId);
    }/**/
}
