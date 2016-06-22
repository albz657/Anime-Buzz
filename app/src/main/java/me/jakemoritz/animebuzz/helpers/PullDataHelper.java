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

    private final static String TAG = PullDataHelper.class.getSimpleName();

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    private Context mContext;

    public void setDelegate(ReadDataResponse delegate) {
        this.delegate = delegate;
    }

    private ReadDataResponse delegate;

    public static PullDataHelper newInstance(SeasonsFragment seasonsFragment){
        PullDataHelper helper = new PullDataHelper();
        helper.setDelegate(seasonsFragment);
        helper.setmContext(seasonsFragment.getContext());
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

    private void handleResponse(JSONObject response){
        ArrayList<Series> seriesFromServer = parseJSON(response);
        delegate.dataRetrieved(seriesFromServer);
//        mAdapter.swapList(seriesFromServer);
    }

    private ArrayList<Series> parseJSON(JSONObject response){
        JsonParser parser = new JsonParser();
        JsonObject gsonObject = (JsonObject) parser.parse(response.toString());

        JsonArray responseSeriesList = gsonObject.getAsJsonArray("items");

        ArrayList<Series> seriesFromServer = new ArrayList<>();
        Iterator iterator = responseSeriesList.iterator();
        JsonObject element;

        int count = 0;
        while (iterator.hasNext()){
            element = (JsonObject) iterator.next();
//            Log.d(TAG, element.toString());
            Log.d(TAG, count + "");
            int MAL_ID = -1;
            try {
                MAL_ID = element.get("MALID").getAsInt();
            } catch (NumberFormatException e){
                // no MAL ID
            }

            if (MAL_ID != -1){
                Series series = new Series(element.get("name").getAsString(),
                        element.get("MALID").getAsInt());
                seriesFromServer.add(series);
            }

            count++;
        }

        return seriesFromServer;
    }
}
