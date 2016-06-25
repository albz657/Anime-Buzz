package me.jakemoritz.animebuzz.helpers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

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

    private ArrayList<Season> parseSeasonList(JSONObject response) {
        try {
            String latestSeasonKey = response.getString("latest");
            String latestSeasonName;
            JSONObject seasonsAsJSON = response.getJSONObject("seasons");

            ArrayList<Season> seasonsList = new ArrayList<>();

            Iterator<String> it = seasonsAsJSON.keys();
            while (it.hasNext()) {
                String key = it.next();
                if (!key.matches("nodate")) {
                    JSONObject seasonAsJSON = seasonsAsJSON.getJSONObject(key);

                    if (key.matches(latestSeasonKey)) {
                        latestSeasonName = seasonAsJSON.getString("name");

                        SharedPreferences settings = activity.getSharedPreferences(activity.getString(R.string.shared_prefs_account), 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(activity.getString(R.string.shared_prefs_latest_season), latestSeasonName);
                        editor.apply();
                    }

                    Season tempSeason = new Season(new DateFormatHelper().getLocalFormattedDateFromStringDate(seasonAsJSON.getString("start_timestamp")),
                            seasonAsJSON.getString("name"),
                            key);

                    seasonsList.add(tempSeason);
                }
            }

            Collections.sort(seasonsList, new SeasonComparator());
            App.getInstance().getSeasonsList().clear();
            App.getInstance().getSeasonsList().addAll(seasonsList);
            App.getInstance().saveSeasonsList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
