package me.jakemoritz.animebuzz.helpers;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.interfaces.ReadSeasonDataResponse;
import me.jakemoritz.animebuzz.interfaces.SenpaiEndpointInterface;
import me.jakemoritz.animebuzz.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SenpaiExportHelper {

    private final static String TAG = SenpaiExportHelper.class.getSimpleName();

    private MainActivity mainActivity;

    public SenpaiExportHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void getSeasonList(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AllSeasonsMetadata.class, new SeasonMetadataDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.senpai.moe/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<AllSeasonsMetadata> call = senpaiEndpointInterface.getSeasonList("json", "seasonlist");
        call.enqueue(new Callback<AllSeasonsMetadata>() {
            @Override
            public void onResponse(Call<AllSeasonsMetadata> call, retrofit2.Response<AllSeasonsMetadata> response) {
                Log.d(TAG, "Success");
                App.getInstance().getSeasonsList().clear();
                App.getInstance().getSeasonsList().addAll(response.body().getMetadataList());

                App.getInstance().saveSeasonsList();

                Log.d(TAG, "Success");
            }

            @Override
            public void onFailure(Call<AllSeasonsMetadata> call, Throwable t) {
                Log.d(TAG, "Failure");
            }
        });
    }

    public void getSeasonData(final SeasonMetadata metadata){
        NotificationHelper helper = new NotificationHelper(App.getInstance());
        helper.createUpdatingSeasonDataNotification(metadata.getName());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Season.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.senpai.moe/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<Season> call = senpaiEndpointInterface.getSeasonData("json", metadata.getKey());
        call.enqueue(new Callback<Season>() {
            @Override
            public void onResponse(Call<Season> call, retrofit2.Response<Season> response) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(metadata.getKey().hashCode());
                Log.d(TAG, "Success");
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(metadata.getKey().hashCode());
                Log.d(TAG, "Failure");
            }
        });
    }

    public void getLatestSeasonData(){
        NotificationHelper helper = new NotificationHelper(App.getInstance());
        helper.createUpdatingSeasonDataNotification("Latest season");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Season.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.senpai.moe/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<Season> call = senpaiEndpointInterface.getSeasonData("json", "raw");
        call.enqueue(new Callback<Season>() {
            @Override
            public void onResponse(Call<Season> call, retrofit2.Response<Season> response) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel("Latest season".hashCode());

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), response.body().getSeasonMetadata().getKey());
                editor.apply();

                App.getInstance().setLatestSeasonKey(response.body().getSeasonMetadata().getKey());

                ReadSeasonDataResponse delegate = mainActivity;
                delegate.seasonDataRetrieved(response.body());
                Log.d(TAG, "Success");
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel("Latest season".hashCode());
                Log.d(TAG, "Failure");
            }
        });
    }
}
