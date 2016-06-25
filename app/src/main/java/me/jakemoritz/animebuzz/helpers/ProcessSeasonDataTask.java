package me.jakemoritz.animebuzz.helpers;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.models.Series;

public class ProcessSeasonDataTask extends AsyncTask<JSONObject, Void, ArrayList<Series>> {

    ReadSeasonDataResponse seasonDataDelegate;

    public ProcessSeasonDataTask(ReadSeasonDataResponse seasonDataDelegate) {
        super();
        this.seasonDataDelegate = seasonDataDelegate;
    }

    private ArrayList<Series> parseSeasonData(JSONObject response) {
        JsonParser parser = new JsonParser();
        JsonObject gsonObject = (JsonObject) parser.parse(response.toString());

        JsonArray responseSeriesList = gsonObject.getAsJsonArray("items");

        JsonObject metadata = gsonObject.getAsJsonObject("meta");
        String season = metadata.get("season").getAsString();

        ArrayList<Series> seriesFromServer = new ArrayList<>();
        Iterator iterator = responseSeriesList.iterator();
        JsonObject seriesAsJSON;

        while (iterator.hasNext()) {
            seriesAsJSON = (JsonObject) iterator.next();

            Series series = new Gson().fromJson(seriesAsJSON, Series.class);
            series.setSeason(season);

            seriesFromServer.add(series);
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
