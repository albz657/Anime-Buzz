package me.jakemoritz.animebuzz.api.senpai;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.gson.SeasonHolder;

class AnimeDeserializer implements JsonDeserializer<SeasonHolder> {

    private static final String TAG = AnimeDeserializer.class.getSimpleName();

    @Override
    public SeasonHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        Realm realm = Realm.getDefaultInstance();

        // Create SeasonMetadata object
        JsonObject metaObject = jsonObject.getAsJsonObject("meta");
        String seasonName = metaObject.get("season").getAsString();

        Season season = realm.where(Season.class).equalTo("name", seasonName).findFirst();
        String seasonKey = season.getKey();
        realm.close();

        SeasonHolder seasonHolder = new SeasonHolder(seasonKey);
        seasonHolder.setSeasonName(seasonName);

        // Parse Series
        JsonArray seriesArray = jsonObject.getAsJsonArray("items");

        final List<Series> seriesList = new ArrayList<>();

        Gson gson = new Gson();
        for (JsonElement seriesElement : seriesArray) {
            JsonObject seriesObject = seriesElement.getAsJsonObject();

            final Series series = gson.fromJson(seriesObject, Series.class);

            if (series != null) {

                if (series.getMALID().matches("^-?\\d+$")) {
                    series.setSeasonKey(seasonKey);

                    seriesList.add(series);

                    boolean missingAirdate = seriesObject.get("missingAirtime").getAsBoolean();
                    int airdate;
                    int simulcast_airdate;
                    if (missingAirdate) {
                        airdate = -1;
                        simulcast_airdate = -1;
                    } else {
                        airdate = seriesObject.get("airdate_u").getAsInt();
                        simulcast_airdate = seriesObject.get("simulcast_airdate_u").getAsInt();
                    }

                    AlarmHelper.getInstance().generateNextEpisodeTimes(series, airdate, simulcast_airdate);
                } else {
                    Log.d(TAG, "'" + series.getName() + "' has no MALID, ignoring");
                }
            }
        }

        seasonHolder.setSeriesList(seriesList);

        return seasonHolder;
    }
}
