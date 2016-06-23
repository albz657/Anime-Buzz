package me.jakemoritz.animebuzz.helpers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.interfaces.ReadDataResponse;
import me.jakemoritz.animebuzz.models.Series;

public class PullDataHelper {

    final static String TAG = PullDataHelper.class.getSimpleName();

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    Context mContext;

    public void setDelegate(ReadDataResponse delegate) {
        this.delegate = delegate;
    }

    ReadDataResponse delegate;

    public static PullDataHelper newInstance(SeasonsFragment fragment) {
        PullDataHelper helper = new PullDataHelper();
        helper.setDelegate(fragment);
        helper.setmContext(fragment.getContext());
        return helper;
    }

    public void getData() {
        Log.d(TAG, "grabbing data");
        // Instantiate RequestQueue
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = "http://www.senpai.moe/export.php?type=json&src=raw";

        // Request JSON response from URL
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                Log.d(TAG, response.toString());
                handleResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }

    void handleResponse(JSONObject response) {
        ArrayList<Series> seriesFromServer = parseJSON(response);
        delegate.dataRetrieved(seriesFromServer);
//        mAdapter.swapList(seriesFromServer);
    }

    ArrayList<Series> parseJSON(JSONObject response) {
        JsonParser parser = new JsonParser();
        JsonObject gsonObject = (JsonObject) parser.parse(response.toString());

        JsonArray responseSeriesList = gsonObject.getAsJsonArray("items");

        ArrayList<Series> seriesFromServer = new ArrayList<>();
        Iterator iterator = responseSeriesList.iterator();
        JsonObject seriesAsJSON;

        int count = 0;
        while (iterator.hasNext()) {
            seriesAsJSON = (JsonObject) iterator.next();

            String title = "";
            int mal_id = -1;
            boolean isSimulcastAired = false;
            boolean isAired = false;
            int airdate = -1;
            int simulcast_airdate = -1;

            Log.d(TAG, count + "");

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
                Series series = new Series(airdate, title, mal_id, isSimulcastAired, isAired, simulcast_airdate);
                seriesFromServer.add(series);
            }

            count++;
        }

        return seriesFromServer;
    }
}
