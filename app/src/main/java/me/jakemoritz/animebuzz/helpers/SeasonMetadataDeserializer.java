package me.jakemoritz.animebuzz.helpers;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.models.SeasonMetadata;

public class SeasonMetadataDeserializer implements JsonDeserializer<AllSeasonsMetadata>{

    private static final String TAG = SeasonMetadataDeserializer.class.getSimpleName();

    @Override
    public AllSeasonsMetadata deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        String latestSeasonKey = jsonObject.get("latest").getAsString();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), latestSeasonKey);
        editor.apply();

        JsonObject seasonsListObject = jsonObject.getAsJsonObject("seasons");

        final List<SeasonMetadata> metadataList = new ArrayList<>();
        for (Map.Entry<String, JsonElement> seasonEntry : seasonsListObject.entrySet()){
            String seasonKey = seasonEntry.getKey();
            JsonObject metadata = seasonEntry.getValue().getAsJsonObject();
            String seasonName = metadata.get("name").getAsString();
            String startTimestamp = metadata.get("start_timestamp").getAsString();

            SeasonMetadata seasonMetadata = new SeasonMetadata(seasonName, startTimestamp, seasonKey);
            metadataList.add(seasonMetadata);
        }

        return new AllSeasonsMetadata(metadataList, latestSeasonKey);
    }
}
