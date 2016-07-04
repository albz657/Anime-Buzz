package me.jakemoritz.animebuzz.api.senpai;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;

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
        if (monthMatcher.find()){
            seasonMonth = monthMatcher.group().toLowerCase();

            Pattern yearPattern = Pattern.compile("(\\d)+");
            Matcher yearMatcher = yearPattern.matcher(seasonName);
            if (yearMatcher.find()){
                seasonYear = yearMatcher.group();
            }
        }
        String seasonKey = seasonMonth + seasonYear;

        final SeasonMetadata seasonMetadata = new SeasonMetadata(seasonName, startTimestamp, seasonKey);
        App.getInstance().getSeasonsList().add(seasonMetadata);

        // Parse Series
        JsonArray seriesArray = jsonObject.getAsJsonArray("items");

        List<Series> seasonSeries = new ArrayList<>();
        for (JsonElement seriesElement : seriesArray){
            JsonObject seriesObject = seriesElement.getAsJsonObject();

            String seriesName = seriesObject.get("name").getAsString();
            int MALID;
            try {
                MALID = seriesObject.get("MALID").getAsInt();
            } catch (NumberFormatException e){
                MALID = -1;
                Log.d(TAG, "'" + seriesName +  "' has no MALID.");
            }
            int ANNID;
            try {
                ANNID = seriesObject.get("ANNID").getAsInt();
            } catch (NumberFormatException e){
                ANNID = -1;
                Log.d(TAG, "'" + seriesName +  "' has no ANNID.");
            }

            boolean missingAirdate = seriesObject.get("missingAirdate").getAsBoolean();
            int airdate;
            int simulcast_airdate;
            if (missingAirdate){
                airdate = -1;
                simulcast_airdate = -1;
                Log.d(TAG, "'" + seriesName +  "' is missing its airdate.");
            } else {
                airdate = seriesObject.get("airdate_u").getAsInt();
                simulcast_airdate = seriesObject.get("simulcast_airdate_u").getAsInt();
            }
            String simulcast;
            try {
                simulcast = seriesObject.get("simulcast").getAsString();
            } catch (ClassCastException e){
                simulcast = "";
                Log.d(TAG, "'" + seriesName +  "' is not simulcast.");
            }
            double simulcast_delay;
            try {
                simulcast_delay = seriesObject.get("simulcast_delay").getAsDouble();
            } catch (NumberFormatException e){
                simulcast_delay = 0;
                if (simulcast.length() == 0){
                    Log.d(TAG, "'" + seriesName + "' is not simulcast).");
                }
            }

            if (MALID > 0){
                final Series series = new Series(airdate, seriesName, MALID, simulcast, simulcast_airdate, seasonName, ANNID, simulcast_delay);
                seasonSeries.add(series);
            }

        }

        final Season season = new Season(seasonSeries, seasonMetadata);
        return season;
    }
}
