package me.jakemoritz.animebuzz.helpers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonComparator;

public class ProcessSeasonListTask extends AsyncTask<JSONObject, Void, ArrayList<Season>> {

    ReadSeasonListResponse seasonListDelegate;
    Activity activity;

    public ProcessSeasonListTask(ReadSeasonListResponse seasonListDelegate, Activity activity) {
        super();
        this.seasonListDelegate = seasonListDelegate;
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(ArrayList<Season> seasons) {
        super.onPostExecute(seasons);
        seasonListDelegate.seasonListReceived(seasons);
    }

    @Override
    protected ArrayList<Season> doInBackground(JSONObject... params) {
        return parseSeasonList(params[0]);
    }

    private ArrayList<Season> parseSeasonList(JSONObject response){
        JsonParser parser = new JsonParser();
        JsonObject gsonObject = (JsonObject) parser.parse(response.toString());

        String latestSeasonTag = gsonObject.get("latest").getAsString();
        String latestSeason;
        JsonObject seasonsAsJSON = gsonObject.getAsJsonObject("seasons");

        Set<Map.Entry<String, JsonElement>> seasonsAsSet = seasonsAsJSON.entrySet();
        ArrayList<Map.Entry<String, JsonElement>> seasonsAsAL = new ArrayList<>(seasonsAsSet);

        ArrayList<Season> seasonsList = new ArrayList<>();

        for (Map.Entry<String, JsonElement> seasonAsElement : seasonsAsAL){

            JsonObject seasonData = (JsonObject) seasonAsElement.getValue();
            String seasonName = seasonData.get("name").getAsString();

            String seasonKey = seasonAsElement.getKey();
            if (!seasonKey.matches("nodate")){
                if (seasonAsElement.getKey().matches(latestSeasonTag)){
                    latestSeason = seasonName;

                    SharedPreferences settings = activity.getSharedPreferences(activity.getString(R.string.shared_prefs_account), 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(activity.getString(R.string.shared_prefs_latest_season), latestSeason);
                    editor.apply();
                }

                String timestampAsDate = seasonData.get("start_timestamp").getAsString();

                String formattedDate = new DateFormatHelper().getLocalFormattedDateFromStringDate(timestampAsDate);

                Season tempSeason = new Season(formattedDate, seasonName, seasonKey);
                seasonsList.add(tempSeason);
            }

        }
        Collections.sort(seasonsList, new SeasonComparator());
        App.getInstance().getSeasonsList().clear();
        App.getInstance().getSeasonsList().addAll(seasonsList);
        App.getInstance().saveSeasonsList();

        return seasonsList;
    }

}
