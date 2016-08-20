package me.jakemoritz.animebuzz.api.senpai;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.senpai.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.retrofit.SenpaiEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import okhttp3.OkHttpClient;
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

    public void getSeasonList() {
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

    public void getSeasonData(final SeasonMetadata metadata) {
        if (App.getInstance().isPostInitializing()) {
            updateNotification(metadata.getName());
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Season.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.senpai.moe/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<Season> call = senpaiEndpointInterface.getSeasonData("json", metadata.getKey());
        call.enqueue(new Callback<Season>() {
            @Override
            public void onResponse(Call<Season> call, retrofit2.Response<Season> response) {
                if (response.isSuccessful()) {
                    if (App.getInstance().isPostInitializing()) {
                        if (App.getInstance().getSyncingSeasons().isEmpty()) {
                            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.cancel(100);

                            App.getInstance().setPostInitializing(false);
                        } else {
                            getSeasonData(App.getInstance().getSyncingSeasons().remove(App.getInstance().getSyncingSeasons().size() - 1));
                        }
                    }

                    fragment.seasonDataRetrieved(response.body());
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

    private void updateNotification(String seasonName) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(App.getInstance().getString(R.string.notification_list_update))
                        .setContentText(seasonName)
                        .setAutoCancel(true)
                        .setProgress(0, 0, true);

        Intent resultIntent = new Intent(App.getInstance(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(App.getInstance());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());
    }

    public void getLatestSeasonData() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Season.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.senpai.moe/")
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<Season> call = senpaiEndpointInterface.getSeasonData("json", "raw");
        call.enqueue(new Callback<Season>() {
            @Override
            public void onResponse(Call<Season> call, retrofit2.Response<Season> response) {
                if (response.isSuccessful()) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(App.getInstance().getString(R.string.shared_prefs_latest_season), response.body().getSeasonMetadata().getName());
                    editor.apply();

                    if (App.getInstance().isInitializing()) {
                        response.body().getSeasonMetadata().setCurrentOrNewer(true);
                        App.getInstance().setCurrentlyBrowsingSeason(response.body());

                    }

                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getInstance());
                    databaseHelper.saveSeasonMetadata(response.body().getSeasonMetadata());

                    fragment.seasonDataRetrieved(response.body());
                }
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                fragment.seasonDataRetrieved(null);
            }
        });
    }
}
