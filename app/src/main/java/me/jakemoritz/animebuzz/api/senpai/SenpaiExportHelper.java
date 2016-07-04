package me.jakemoritz.animebuzz.api.senpai;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.senpai.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.interfaces.SenpaiEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SenpaiExportHelper {

    private final static String TAG = SenpaiExportHelper.class.getSimpleName();

    private SeriesFragment fragment;

    public SenpaiExportHelper(SeriesFragment fragment) {
        this.fragment = fragment;
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
                if (response.isSuccessful()) {
                    App.getInstance().getSeasonsList().addAll(response.body().getMetadataList());
                    App.getInstance().saveSeasonsList();
                    fragment.seasonListReceived(response.body().getMetadataList());

                    Log.d(TAG, "Got season list");
                }
            }

            @Override
            public void onFailure(Call<AllSeasonsMetadata> call, Throwable t) {
                Log.d(TAG, "Failed getting season list");
            }
        });
    }

    public void getSeasonData(final SeasonMetadata metadata){
        NotificationHelper helper = new NotificationHelper(App.getInstance());
        //helper.createUpdatingSeasonDataNotification(metadata.getName());

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
                if (response.isSuccessful()) {
                    NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(metadata.getName().hashCode());

                    fragment.seasonDataRetrieved(response.body());
                    Log.d(TAG, "Got season data for: '" + metadata.getName() + "'");
                }
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(metadata.getName().hashCode());
                Log.d(TAG, "Failed getting season data for: '" + metadata.getName() + "'");
            }
        });
    }

    public void getLatestSeasonData(){
        NotificationHelper helper = new NotificationHelper(App.getInstance());
        //helper.createUpdatingSeasonDataNotification("Latest season");

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
                if (response.isSuccessful()) {
                    NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel("Latest season".hashCode());

                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), response.body().getSeasonMetadata().getName());
                    editor.apply();

                    App.getInstance().setCurrentlyBrowsingSeasonName(response.body().getSeasonMetadata().getName());

                    DatabaseHelper databaseHelper = new DatabaseHelper(App.getInstance());
                    databaseHelper.saveSeasonMetadataToDb(response.body().getSeasonMetadata());

                    fragment.seasonDataRetrieved(response.body());
                    Log.d(TAG, "Got latest season data");
                }
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel("Latest season".hashCode());
                Log.d(TAG, "Failed getting latest season data");
            }
        });
    }
}
