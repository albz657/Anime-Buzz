package me.jakemoritz.animebuzz.helpers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.SeasonMeta;
import me.jakemoritz.animebuzz.models.SeasonComparator;

public class ProcessSeasonListTask extends AsyncTask<JSONObject, Void, ArrayList<SeasonMeta>> {

    ReadSeasonListResponse seasonListDelegate;
    Activity activity;

    public ProcessSeasonListTask(ReadSeasonListResponse seasonListDelegate, Activity activity) {
        super();
        this.seasonListDelegate = seasonListDelegate;
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(ArrayList<SeasonMeta> seasonMetas) {
        super.onPostExecute(seasonMetas);
        seasonListDelegate.seasonListReceived(seasonMetas);
    }

    @Override
    protected ArrayList<SeasonMeta> doInBackground(JSONObject... params) {
        return parseSeasonList(params[0]);
    }

    private ArrayList<SeasonMeta> parseSeasonList(JSONObject response) {
        try {
            String latestSeasonKey = response.getString("latest");
            String latestSeasonName;
            JSONObject seasonsAsJSON = response.getJSONObject("seasons");

            ArrayList<SeasonMeta> seasonsList = new ArrayList<>();

            Iterator<String> it = seasonsAsJSON.keys();
            while (it.hasNext()) {
                String key = it.next();
                if (!key.matches("nodate")) {
                    JSONObject seasonAsJSON = seasonsAsJSON.getJSONObject(key);

                    if (key.matches(latestSeasonKey)) {
                        latestSeasonName = seasonAsJSON.getString("name");

                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(activity.getString(R.string.shared_prefs_latest_season), latestSeasonName);
                        editor.apply();
                    }

                    SeasonMeta tempSeasonMeta = new SeasonMeta(new DateFormatHelper().getLocalFormattedDateFromStringDate(seasonAsJSON.getString("start_timestamp")),
                            seasonAsJSON.getString("name"),
                            key);

                    seasonsList.add(tempSeasonMeta);
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
