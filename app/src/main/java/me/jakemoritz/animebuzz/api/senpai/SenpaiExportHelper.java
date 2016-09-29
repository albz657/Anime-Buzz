package me.jakemoritz.animebuzz.api.senpai;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.senpai.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
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

    private final static String BASE_URL = "http://www.senpai.moe/";

    private SeriesFragment fragment;

    public SenpaiExportHelper(SeriesFragment fragment) {
        this.fragment = fragment;
    }

    public void getSeasonList() {
        Log.d(TAG, "Getting season list");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AllSeasonsMetadata.class, new SeasonMetadataDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
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
                } else {
                    fragment.seasonListReceived(null);

                }
            }

            @Override
            public void onFailure(Call<AllSeasonsMetadata> call, Throwable t) {
                Log.d(TAG, "Failed getting season list");
                fragment.seasonListReceived(null);
            }
        });
    }

    public void getSeasonData(final SeasonMetadata metadata) {
        Log.d(TAG, "Getting season data for: '" + metadata.getName() + "'");

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
                .baseUrl(BASE_URL)
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
                } else {
                    NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(metadata.getName().hashCode());
                    Log.d(TAG, "Failed getting season data for: '" + metadata.getName() + "'");

                    fragment.seasonDataRetrieved(null);
                }
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(metadata.getName().hashCode());
                Log.d(TAG, "Failed getting season data for: '" + metadata.getName() + "'");

                fragment.seasonDataRetrieved(null);
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
        Log.d(TAG, "Getting latest season data");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Season.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

   /*     OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();*/

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<Season> call = senpaiEndpointInterface.getSeasonData("json", "raw");
        call.enqueue(new Callback<Season>() {
            @Override
            public void onResponse(Call<Season> call, retrofit2.Response<Season> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Got latest season data");

                    SharedPrefsHelper.getInstance().setLatestSeasonName(response.body().getSeasonMetadata().getName());

                    App.getInstance().removeOlderShows();

                    response.body().getSeasonMetadata().setCurrentOrNewer(true);
                    App.getInstance().setCurrentlyBrowsingSeason(response.body());

                    fragment.seasonDataRetrieved(response.body());
                } else {
                    Log.d(TAG, "Failed getting latest season data");

                    fragment.seasonDataRetrieved(null);
                }
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                Log.d(TAG, "Failed getting latest season data");

                fragment.seasonDataRetrieved(null);
            }
        });
    }
}
