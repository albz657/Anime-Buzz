package me.jakemoritz.animebuzz.api.senpai;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.api.senpai.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.comparators.SeasonComparator;
import me.jakemoritz.animebuzz.models.Season;

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

                realm.beginTransaction();

                Season season = realm.where(Season.class).equalTo("key", seasonKey).findFirst();

                if (season == null){
                    realm.beginTransaction();

                    season = realm.createObject(Season.class);
                    season.setName(seasonName);
                    season.setKey(seasonKey);
                    season.setStartDate(startTimestamp);

                    App.getInstance().getAllAnimeSeasons().add(season);
                    Collections.sort(App.getInstance().getAllAnimeSeasons(), new SeasonComparator());
                    season.setChronologicalIndex(App.getInstance().getAllAnimeSeasons().indexOf(season));

                    realm.commitTransaction();
                }

                realm.commitTransaction();
                metadataList.add(season);
            }
        }
        return new AllSeasonsMetadata(metadataList, latestSeasonKey);
    }
}
