package me.jakemoritz.animebuzz.helpers;

import android.os.AsyncTask;

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

            String title = "";
            int mal_id = -1;
            boolean isSimulcastAired = false;
            boolean isAired = false;
            int airdate = -1;
            int simulcast_airdate = -1;
            boolean currentlyAiring;

            try {
                mal_id = seriesAsJSON.get("MALID").getAsInt();
                title = seriesAsJSON.get("name").getAsString();
                isSimulcastAired = seriesAsJSON.get("isSimulcastAired").getAsBoolean();
                isAired = seriesAsJSON.get("isAired").getAsBoolean();
                airdate = seriesAsJSON.get("airdate_u").getAsInt();
                simulcast_airdate = seriesAsJSON.get("simulcast_airdate_u").getAsInt();
            } catch (NumberFormatException e) {
                // no MAL ID
            }

            if (mal_id != -1) {
                Series series = new Series(airdate, title, mal_id, isSimulcastAired, isAired, simulcast_airdate, false, season, false);
                seriesFromServer.add(series);
            }
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
