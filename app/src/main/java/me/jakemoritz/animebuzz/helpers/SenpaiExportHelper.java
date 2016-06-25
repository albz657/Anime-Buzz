package me.jakemoritz.animebuzz.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class SenpaiExportHelper {

    final static String TAG = SenpaiExportHelper.class.getSimpleName();

    Context mContext;
    ReadSeasonDataResponse delegate;

    public static SenpaiExportHelper newInstance(SeasonsFragment fragment) {
        SenpaiExportHelper helper = new SenpaiExportHelper();
        helper.setDelegate(fragment);
        helper.setmContext(fragment.getContext());
        return helper;
    }

    public void setDelegate(ReadSeasonDataResponse delegate) {
        this.delegate = delegate;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void getSeasonList() {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = "http://www.senpai.moe/export.php?type=json&src=seasonlist";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseSeasonList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void getSeasonData(Season season) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        String base = "http://www.senpai.moe/export.php?type=json";
        Uri uri = Uri.parse(base);
        Uri.Builder builder = uri.buildUpon()
                .appendQueryParameter("src", season.getKey());
        String url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ArrayList<Series> seriesFromServer = parseSeasonData(response);
                delegate.seasonDataRetrieved(seriesFromServer);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void parseSeasonList(JSONObject response){
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

                    SharedPreferences settings = mContext.getSharedPreferences(mContext.getString(R.string.shared_prefs_account), 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(mContext.getString(R.string.shared_prefs_latest_season), latestSeason);

                    editor.apply();
                }

                String timestampAsDate = seasonData.get("start_timestamp").getAsString();

                String formattedDate = new DateFormatHelper().getLocalFormattedDateFromStringDate(timestampAsDate);

                Season tempSeason = new Season(formattedDate, seasonName, seasonKey);
                seasonsList.add(tempSeason);
            }

        }

        App.getInstance().getSeasonsList().clear();
        App.getInstance().getSeasonsList().addAll(seasonsList);
        App.getInstance().saveSeasonsList();
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
}
