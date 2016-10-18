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
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Season;

class SeasonMetadataDeserializer implements JsonDeserializer<RealmList<Season>>{

    private static final String TAG = SeasonMetadataDeserializer.class.getSimpleName();

    @Override
    public RealmList<Season> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        JsonObject seasonsListObject = jsonObject.getAsJsonObject("seasons");

        RealmList<Season> metadataList = new RealmList<>();
        Realm realm = Realm.getDefaultInstance();
        for (Map.Entry<String, JsonElement> seasonEntry : seasonsListObject.entrySet()){
            String seasonKey = seasonEntry.getKey();

            if (!seasonKey.equals("nodate")){
                JsonObject metadata = seasonEntry.getValue().getAsJsonObject();
                String seasonName = metadata.get("name").getAsString();
                String startTimestamp = metadata.get("start_timestamp").getAsString();

                if (!seasonName.equals(SharedPrefsHelper.getInstance().getLatestSeasonName())){
                    final Season season = new Season();
                    season.setKey(seasonKey);
                    season.setName(seasonName);
                    season.setStartDate(startTimestamp);
                    season.setRelativeTime(Season.calculateRelativeTime(seasonName));

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(season);
                        }
                    });

                    metadataList.add(season);
                }

            }
        }
        realm.close();
        return metadataList;
    }
}
