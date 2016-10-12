package me.jakemoritz.animebuzz.api.senpai;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.api.senpai.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

class SeasonMetadataDeserializer implements JsonDeserializer<AllSeasonsMetadata>{

    private static final String TAG = SeasonMetadataDeserializer.class.getSimpleName();

    @Override
    public AllSeasonsMetadata deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        String latestSeasonKey = jsonObject.get("latest").getAsString();

        JsonObject seasonsListObject = jsonObject.getAsJsonObject("seasons");

        final RealmList<Season> metadataList = new RealmList<>();
        Realm realm = Realm.getDefaultInstance();
        for (Map.Entry<String, JsonElement> seasonEntry : seasonsListObject.entrySet()){
            String seasonKey = seasonEntry.getKey();

            if (!seasonKey.equals("nodate")){
                JsonObject metadata = seasonEntry.getValue().getAsJsonObject();
                String seasonName = metadata.get("name").getAsString();
                String startTimestamp = metadata.get("start_timestamp").getAsString();

                Season season = realm.where(Season.class).equalTo("key", seasonKey).findFirst();

                if (season == null) {
                    season = new Season();
                }

                season.setKey(seasonKey);
                season.setName(seasonName);
                season.setStartDate(startTimestamp);
                season.setRelativeTime(Season.calculateRelativeTime(seasonName));
                season.setSeasonSeries(new RealmList<Series>());

                metadataList.add(season);
            }
        }
        realm.close();
        return new AllSeasonsMetadata(metadataList, latestSeasonKey);
    }
}
