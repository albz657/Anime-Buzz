package me.jakemoritz.animebuzz.api.senpai.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.Map;

import io.realm.Realm;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.models.Season;

public class SeasonDeserializer implements JsonDeserializer<String> {

    private static final String TAG = SeasonDeserializer.class.getSimpleName();

    @Override
    public String deserialize(final JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final JsonPrimitive latestSeasonJson = jsonObject.getAsJsonPrimitive("latest");
        String latestSeasonKey = latestSeasonJson.getAsString();
        SharedPrefsUtils.getInstance().setLatestSeasonKey(latestSeasonKey);

        final JsonObject seasonsListObject = jsonObject.getAsJsonObject("seasons");

        Realm realm = Realm.getDefaultInstance();

        for (final Map.Entry<String, JsonElement> seasonEntry : seasonsListObject.entrySet()) {
            String seasonKey = seasonEntry.getKey();

            if (!seasonKey.equals("nodate")) {
                JsonObject metadata = seasonEntry.getValue().getAsJsonObject();
                String seasonName = metadata.get("name").getAsString();
                String startTimestamp = metadata.get("start_timestamp").getAsString();

                if (seasonKey.equals(SharedPrefsUtils.getInstance().getLatestSeasonKey())) {
                    SharedPrefsUtils.getInstance().setLatestSeasonName(seasonName);
                }

                final Season season = new Season();
                season.setKey(seasonKey);
                season.setName(seasonName);
                season.setStart_timestamp(startTimestamp);
                season.setRelativeTime(Season.calculateRelativeTime(seasonName));

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(season);
                    }
                });
            }
        }
        realm.close();
        return "";
    }
}
