package me.jakemoritz.animebuzz.api.senpai.deserializers;

import android.util.Log;

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
import me.jakemoritz.animebuzz.utils.AlarmUtils;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.api.senpai.models.SeasonHolder;

public class AnimeDeserializer implements JsonDeserializer<SeasonHolder> {

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

        SeasonHolder seasonHolder = new SeasonHolder(seasonKey);
        seasonHolder.setSeasonName(seasonName);

        // Parse Series
        JsonArray seriesArray = jsonObject.getAsJsonArray("items");

        final List<Series> seriesList = new ArrayList<>();

        for (JsonElement seriesElement : seriesArray) {
            JsonObject seriesObject = seriesElement.getAsJsonObject();

            String seriesName = seriesObject.get("name").getAsString();

            String MALID = null;
            try {
                MALID = String.valueOf(seriesObject.get("MALID").getAsInt());

                Series series = new Series();
                series.setMALID(MALID);

                Series realmSeries = realm.where(Series.class).equalTo("MALID", series.getMALID()).findFirst();
                if (realmSeries != null) {
                    series.duplicateRealmValues(realmSeries);
                }

                String ANNID;
                try {
                    ANNID = seriesObject.get("ANNID").getAsString();
                } catch (NumberFormatException e) {
                    ANNID = "";
//                    Log.d(TAG, "'" + seriesName + "' has no ANNID.");
                }

                String simulcast;
                try {
                    simulcast = seriesObject.get("simulcast").getAsString();
                } catch (ClassCastException e) {
                    simulcast = "";
                    Log.d(TAG, "'" + seriesName + "' is not simulcast.");
                }

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

                series.setName(seriesName);
                series.setSeasonKey(seasonKey);
                series.setSimulcastProvider(simulcast);
                series.setANNID(ANNID);

                AlarmUtils.getInstance().generateNextEpisodeTimes(series, airdate, simulcast_airdate);

                seriesList.add(series);
            } catch (NumberFormatException e) {
                Log.d(TAG, "'" + seriesName + "' has no MALID, ignoring");
            }
        }
        realm.close();

        seasonHolder.setSeriesList(seriesList);

        return seasonHolder;
    }
}
