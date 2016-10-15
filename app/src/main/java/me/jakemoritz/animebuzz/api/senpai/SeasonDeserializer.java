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

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

class SeasonDeserializer implements JsonDeserializer<String> {

    private static final String TAG = SeasonDeserializer.class.getSimpleName();

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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

        Realm realm = Realm.getDefaultInstance();
        Season season = realm.where(Season.class).equalTo("key", seasonKey).findFirst();

        if (season == null) {
            realm.beginTransaction();
            season = realm.createObject(Season.class, seasonKey);
            realm.commitTransaction();
        }

        realm.beginTransaction();
        season.setName(seasonName);
        season.setStartDate(startTimestamp);
        season.setRelativeTime(Season.calculateRelativeTime(seasonName));
        realm.commitTransaction();

        if (App.getInstance().isInitializing() && SharedPrefsHelper.getInstance().getLatestSeasonName().isEmpty()) {
            SharedPrefsHelper.getInstance().setLatestSeasonName(seasonName);
            SharedPrefsHelper.getInstance().setLatestSeasonKey(seasonKey);
        }

        // Parse Series
        JsonArray seriesArray = jsonObject.getAsJsonArray("items");

        RealmList<Series> seasonSeries = new RealmList<>();
        for (JsonElement seriesElement : seriesArray) {
            JsonObject seriesObject = seriesElement.getAsJsonObject();

            String seriesName = seriesObject.get("name").getAsString();
            String MALID = null;
            try {
                MALID = seriesObject.get("MALID").getAsString();
            } catch (NumberFormatException e) {
                Log.d(TAG, "'" + seriesName + "' has no MALID, ignoring");
            }

            if (MALID != null && !MALID.equals("false")) {
                String ANNID;
                try {
                    ANNID = seriesObject.get("ANNID").getAsString();
                } catch (NumberFormatException e) {
                    ANNID = "";
//                    Log.d(TAG, "'" + seriesName + "' has no ANNID.");
                }

                boolean missingAirdate = seriesObject.get("missingAirtime").getAsBoolean();
                int airdate;
                int simulcast_airdate;
                if (missingAirdate) {
                    airdate = -1;
                    simulcast_airdate = -1;
//                    Log.d(TAG, "'" + seriesName + "' is missing its airdate.");
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

                Series series = realm.where(Series.class).equalTo("MALID", MALID).findFirst();

                if (series == null) {
                    realm.beginTransaction();
                    series = realm.createObject(Series.class, MALID);
                    series.setName(seriesName);
                    realm.commitTransaction();
                }

                realm.beginTransaction();
                series.setANNID(ANNID);
                series.setSimulcastProvider(simulcast);
                series.setSeason(season);
                series.setSimulcast_delay(simulcast_delay);
                realm.commitTransaction();

                if (seasonName.equals(SharedPrefsHelper.getInstance().getLatestSeasonName())) {
                    AlarmHelper.getInstance().generateNextEpisodeTimes(series, airdate, simulcast_airdate);
                    SharedPrefsHelper.getInstance().setLastUpdateTime(Calendar.getInstance().getTimeInMillis());
                }

                seasonSeries.add(series);

            }

        }

        realm.beginTransaction();
        realm.copyToRealm(seasonSeries);
        season.setSeasonSeries(seasonSeries);
        realm.commitTransaction();

        realm.close();
        return seasonKey;
    }
}
