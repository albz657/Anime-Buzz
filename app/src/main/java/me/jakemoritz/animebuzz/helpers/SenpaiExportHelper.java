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
import me.jakemoritz.animebuzz.interfaces.SenpaiEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SenpaiExportHelper {

    private final static String TAG = SenpaiExportHelper.class.getSimpleName();

    private static final String SEASON_LIST = "http://www.senpai.moe/export.php?type=json&src=seasonlist";
    private static final String SEASON_DATA_BASE = "http://www.senpai.moe/export.php?type=json";
    private static final String LATEST_SEASON_DATA = "http://www.senpai.moe/export.php?type=json&src=raw";


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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, SEASON_LIST, null, new Response.Listener<JSONObject>() {
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
        Uri uri = Uri.parse(SEASON_DATA_BASE);
        Uri.Builder builder = uri.buildUpon()
                .appendQueryParameter("src", season.getKey());
        String url = builder.build().toString();

        NotificationHelper helper = new NotificationHelper(activity);
        helper.createUpdatingSeasonDataNotification(season.getName());

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

    public void getLatestSeasonData() {
        NotificationHelper helper = new NotificationHelper(activity);
        helper.createUpdatingSeasonDataNotification("Latest season");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, LATEST_SEASON_DATA, null, new Response.Listener<JSONObject>() {
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

    public void getLatestSeasonDataRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.senpai.moe/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<Series> call = senpaiEndpointInterface.getLatestSeasonData("json", "raw");
        call.enqueue(new Callback<Series>() {
            @Override
            public void onResponse(Call<Series> call, retrofit2.Response<Series> response) {
                Log.d(TAG,  response.toString());

            }

            @Override
            public void onFailure(Call<Series> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }
}
