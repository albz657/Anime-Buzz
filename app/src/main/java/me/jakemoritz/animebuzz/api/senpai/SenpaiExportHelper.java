package me.jakemoritz.animebuzz.api.senpai;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.Realm;
import me.jakemoritz.animebuzz.api.senpai.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.retrofit.SenpaiEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
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
                    Realm realm = Realm.getDefaultInstance();

                    for (Season season : response.body().getMetadataList()){
                        realm.beginTransaction();
                        realm.copyToRealm(season);
                        realm.commitTransaction();
                    }

                    realm.close();

                    fragment.senpaiSeasonListReceived(response.body().getMetadataList());

                    Log.d(TAG, "Got season list");
                } else {
                    fragment.senpaiSeasonListReceived(null);
                }
            }

            @Override
            public void onFailure(Call<AllSeasonsMetadata> call, Throwable t) {
                Log.d(TAG, "Failed getting season list");
                fragment.senpaiSeasonListReceived(null);
            }
        });
    }

    public void getSeasonData(final Season season) {
        Log.d(TAG, "Getting season data for: '" + season.getName() + "'");

        if (App.getInstance().isPostInitializing() && !App.getInstance().isGettingPostInitialImages()) {
            NotificationHelper.getInstance().createSeasonDataNotification(season.getName());
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Season.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

/*        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();*/

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<Season> call = senpaiEndpointInterface.getSeasonData("json", season.getKey());
        call.enqueue(new Callback<Season>() {
            @Override
            public void onResponse(Call<Season> call, retrofit2.Response<Season> response) {
                if (response.isSuccessful()) {
                    if (App.getInstance().isPostInitializing()) {
                        if (App.getInstance().getSyncingSeasons().isEmpty()) {
                            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.cancel(100);

//                            App.getInstance().setPostInitializing(false);
                        } else {
                            getSeasonData(App.getInstance().getSyncingSeasons().remove(App.getInstance().getSyncingSeasons().size() - 1));
                        }
                    }

                    fragment.senpaiSeasonRetrieved(response.body());
                } else {
                    NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(100);
                    Log.d(TAG, "Failed getting season data for: '" + season.getName() + "'");

                    fragment.senpaiSeasonRetrieved(null);
                }
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(100);
                Log.d(TAG, "Failed getting season data for: '" + season.getName() + "'");

                fragment.senpaiSeasonRetrieved(null);
            }
        });
    }



    public void getLatestSeasonData() {
        Log.d(TAG, "Getting latest season data");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Season.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

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

                    Season season = response.body();
                    season.setRelativeTime(Season.PRESENT);

                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    realm.copyToRealm(season);

                    realm.commitTransaction();

                    realm.close();

                    SharedPrefsHelper.getInstance().setLatestSeasonName(season.getName());
                    SharedPrefsHelper.getInstance().setLatestSeasonKey(season.getKey());

                    fragment.senpaiSeasonRetrieved(response.body());
                } else {
                    Log.d(TAG, "Failed getting latest season data");

                    fragment.senpaiSeasonRetrieved(null);
                }
            }

            @Override
            public void onFailure(Call<Season> call, Throwable t) {
                Log.d(TAG, "Failed getting latest season data");

                fragment.senpaiSeasonRetrieved(null);
            }
        });
    }
}
