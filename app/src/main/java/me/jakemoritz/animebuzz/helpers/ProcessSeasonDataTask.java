package me.jakemoritz.animebuzz.helpers;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.models.Series;

public class ProcessSeasonDataTask extends AsyncTask<JSONObject, Void, ArrayList<Series>> {

    ReadSeasonDataResponse seasonDataDelegate;

    public ProcessSeasonDataTask(ReadSeasonDataResponse seasonDataDelegate) {
        super();
        this.seasonDataDelegate = seasonDataDelegate;
    }

    private ArrayList<Series> parseSeasonData(JSONObject response) {
        ArrayList<Series> seriesFromServer = new ArrayList<>();

        try {
            JSONArray testResponse = response.getJSONArray("items");
            JSONObject meta = response.getJSONObject("meta");
            String season = meta.getString("season");
            Log.d("s", "s");
            for (int i = 0; i < testResponse.length(); i++) {
                JSONObject seriesAsJSON = testResponse.getJSONObject(i);
                //Series series = gson.fromJson(parser.parse(seriesAsJSON.toString()), Series.class);
                Series series = new Series(seriesAsJSON.getInt("airdate_u"),
                        seriesAsJSON.getString("name"),
                        seriesAsJSON.getInt("MALID"),
                        seriesAsJSON.getInt("simulcast_airdate_u"),
                        false,
                        season,
                        false);

                seriesFromServer.add(series);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return seriesFromServer;
    }

    @Override
    protected void onPostExecute(ArrayList<Series> series) {
        super.onPostExecute(series);
        seasonDataDelegate.seasonDataRetrieved(series);
    }

    @Override
    protected ArrayList<Series> doInBackground(JSONObject... params) {
        return parseSeasonData(params[0]);
    }
}
