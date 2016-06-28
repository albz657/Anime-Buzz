package me.jakemoritz.animebuzz.helpers;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonListResponse;
import me.jakemoritz.animebuzz.models.Season;

public class SenpaiExportHelper {

    private final static String TAG = SenpaiExportHelper.class.getSimpleName();

    private MainActivity activity;
    private ReadSeasonDataResponse seasonDataDelegate;
    private RequestQueue queue;
    private ReadSeasonListResponse seasonListDelegate;

    public SenpaiExportHelper(MainActivity activity) {
        this.activity = activity;
        this.seasonListDelegate = activity;
        this.seasonDataDelegate = activity;
        this.queue = Volley.newRequestQueue(activity);
    }

    public void getSeasonList() {
        String url = "http://www.senpai.moe/export.php?type=json&src=seasonlist";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ProcessSeasonListTask processTask = new ProcessSeasonListTask(seasonListDelegate, activity);
                processTask.execute(response);
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
        String base = "http://www.senpai.moe/export.php?type=json";
        Uri uri = Uri.parse(base);
        Uri.Builder builder = uri.buildUpon()
                .appendQueryParameter("src", season.getKey());
        String url = builder.build().toString();

        NotificationHelper helper = new NotificationHelper(activity);
        helper.createUpdatingSeasonDataNotification(season);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ProcessSeasonDataTask processTask = new ProcessSeasonDataTask(seasonDataDelegate);
                processTask.execute(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }


}
