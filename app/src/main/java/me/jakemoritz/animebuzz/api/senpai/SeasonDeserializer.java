package me.jakemoritz.animebuzz.api.senpai;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SeasonDeserializer implements JsonDeserializer<Season> {

    private static final String TAG = SeasonDeserializer.class.getSimpleName();

    @Override
    public Season deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        // Create SeasonMetadata object
        JsonObject metaObject = jsonObject.getAsJsonObject("meta");
        String seasonName = metaObject.get("season").getAsString();
        String startTimestamp = metaObject.get("start").getAsString();

        Pattern monthPattern = Pattern.compile("(\\w\\D)*");
        Matcher monthMatcher = monthPattern.matcher(seasonName);
        String seasonMonth = "";
        String seasonYear = "";
        if (monthMatcher.find()) {
            seasonMonth = monthMatcher.group().toLowerCase();

            Pattern yearPattern = Pattern.compile("(\\d)+");
            Matcher yearMatcher = yearPattern.matcher(seasonName);
            if (yearMatcher.find()) {
                seasonYear = yearMatcher.group();
            }
        }
        String seasonKey = seasonMonth + seasonYear;

        final SeasonMetadata seasonMetadata = new SeasonMetadata(seasonName, startTimestamp, seasonKey);
        if (App.getInstance().getSeasonsList().add(seasonMetadata)){
            seasonMetadata.save();
        }

        if (App.getInstance().isInitializing() && SharedPrefsHelper.getInstance().getLatestSeasonName().isEmpty()) {
            SharedPrefsHelper.getInstance().setLatestSeasonName(seasonMetadata.getName());
        }

        // Parse Series
        JsonArray seriesArray = jsonObject.getAsJsonArray("items");

        SeriesList seasonSeries = new SeriesList();
        for (JsonElement seriesElement : seriesArray) {
            JsonObject seriesObject = seriesElement.getAsJsonObject();

            String seriesName = seriesObject.get("name").getAsString();
            Long MALID;
            try {
                MALID = seriesObject.get("MALID").getAsLong();
            } catch (NumberFormatException e) {
                MALID = -1L;
                Log.d(TAG, "'" + seriesName + "' has no MALID, ignoring");
            }

            if (MALID > 0) {
                int ANNID;
                try {
                    ANNID = seriesObject.get("ANNID").getAsInt();
                } catch (NumberFormatException e) {
                    ANNID = -1;
                    Log.d(TAG, "'" + seriesName + "' has no ANNID.");
                }

                boolean missingAirdate = seriesObject.get("missingAirdate").getAsBoolean();
                int airdate;
                int simulcast_airdate;
                if (missingAirdate) {
                    airdate = -1;
                    simulcast_airdate = -1;
                    Log.d(TAG, "'" + seriesName + "' is missing its airdate.");
                } else {
                    airdate = seriesObject.get("airdate_u").getAsInt();
                    simulcast_airdate = seriesObject.get("simulcast_airdate_u").getAsInt();
                }
                String simulcast;
                try {
                    simulcast = seriesObject.get("simulcast").getAsString();
                } catch (ClassCastException e) {
                    simulcast = "";
                    Log.d(TAG, "'" + seriesName + "' is not simulcast.");
                }
                double simulcast_delay;
                try {
                    simulcast_delay = seriesObject.get("simulcast_delay").getAsDouble();
                } catch (NumberFormatException e) {
                    simulcast_delay = 0;
                    if (simulcast.length() == 0) {
                        Log.d(TAG, "'" + seriesName + "' is not simulcast).");
                    }
                }

                Series series = new Series(airdate, seriesName, MALID, simulcast, simulcast_airdate, seasonName, ANNID, simulcast_delay);

                if (seasonName.equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {

                    App.getInstance().generateNextEpisodeTimes(series, true);
                    App.getInstance().generateNextEpisodeTimes(series, false);

                    SharedPrefsHelper.getInstance().setLastUpdateTime(Calendar.getInstance().getTimeInMillis());
                }

                seasonSeries.add(series);
            }

        }

        return new Season(seasonSeries, seasonMetadata);
    }
}
