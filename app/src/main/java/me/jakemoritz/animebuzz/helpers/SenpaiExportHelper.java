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

    final static String TAG = SenpaiExportHelper.class.getSimpleName();

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    MainActivity activity;
    ReadSeasonDataResponse seasonDataDelegate;

    public void setSeasonListDelegate(ReadSeasonListResponse seasonListDelegate) {
        this.seasonListDelegate = seasonListDelegate;
    }

    ReadSeasonListResponse seasonListDelegate;

    public static SenpaiExportHelper newInstance(MainActivity activity) {
        SenpaiExportHelper helper = new SenpaiExportHelper();
        helper.setActivity(activity);
        helper.setSeasonDataDelegate(activity);
        helper.setSeasonListDelegate(activity);
        return helper;
    }

    public void setSeasonDataDelegate(ReadSeasonDataResponse seasonDataDelegate) {
        this.seasonDataDelegate = seasonDataDelegate;
    }

    public void getSeasonList() {
        RequestQueue queue = Volley.newRequestQueue(activity);
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
        RequestQueue queue = Volley.newRequestQueue(activity);

        String base = "http://www.senpai.moe/export.php?type=json";
        Uri uri = Uri.parse(base);
        Uri.Builder builder = uri.buildUpon()
                .appendQueryParameter("src", season.getKey());
        String url = builder.build().toString();

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
