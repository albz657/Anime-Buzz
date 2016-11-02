package me.jakemoritz.animebuzz.api.senpai;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.dialogs.FailedInitializationFragment;
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

        RealmList<Season> typeList = new RealmList<>();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(typeList.getClass(), new SeasonMetadataDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<RealmList<Season>> call = senpaiEndpointInterface.getSeasonList("json", "seasonlist");
        call.enqueue(new Callback<RealmList<Season>>() {
            @Override
            public void onResponse(Call<RealmList<Season>> call, retrofit2.Response<RealmList<Season>> response) {
                if (response.isSuccessful()) {
                    fragment.senpaiSeasonListReceived();

                    Log.d(TAG, "Got season list");
                } else {
                    if (App.getInstance().isInitializing()){
                        FailedInitializationFragment failedInitializationFragment = FailedInitializationFragment.newInstance(fragment);
                        failedInitializationFragment.show(fragment.getMainActivity().getFragmentManager(), TAG);
                    }
                }
            }

            @Override
            public void onFailure(Call<RealmList<Season>> call, Throwable t) {
                Log.d(TAG, "Failed getting season list");
                if (App.getInstance().isInitializing()){
                    FailedInitializationFragment failedInitializationFragment = FailedInitializationFragment.newInstance(fragment);
                    failedInitializationFragment.show(fragment.getMainActivity().getFragmentManager(), TAG);
                }
            }
        });
    }

    public void getSeasonData(final Season season) {
        Log.d(TAG, "Getting season data for: '" + season.getName() + "'");

        if (App.getInstance().isPostInitializing()) {
            NotificationHelper.getInstance().createSeasonDataNotification(season.getName());
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(String.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<String> call = senpaiEndpointInterface.getSeasonData("json", season.getKey());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body().equals(SharedPrefsHelper.getInstance().getLatestSeasonKey())){
                        SharedPrefsHelper.getInstance().setLastUpdateTime(System.currentTimeMillis());
                    }

                    if (App.getInstance().isPostInitializing()) {
                        if (App.getInstance().getSyncingSeasons() == null || App.getInstance().getSyncingSeasons().isEmpty()) {
                            NotificationManager mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.cancel(100);

                            App.getInstance().setPostInitializing(false);
                        } else {
                            getSeasonData(App.getInstance().getSyncingSeasons().remove(App.getInstance().getSyncingSeasons().size() - 1));
                        }
                    }

                    fragment.senpaiSeasonRetrieved(response.body());
                } else {
                    NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(100);
                    Log.d(TAG, "Failed getting season data for: '" + "" + "'");

                    fragment.senpaiSeasonRetrieved(null);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NotificationManager manager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(100);
                Log.d(TAG, "Failed getting season data for: '" + "" + "'");

                fragment.senpaiSeasonRetrieved(null);
            }
        });
    }

    public void getLatestSeasonData() {
        Log.d(TAG, "Getting latest season data");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(String.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<String> call = senpaiEndpointInterface.getSeasonData("json", "raw");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Got latest season data");

                    fragment.senpaiSeasonRetrieved(response.body());
                } else {
                    Log.d(TAG, "Failed getting latest season data");

                    fragment.senpaiSeasonRetrieved(null);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "Failed getting latest season data");

                fragment.senpaiSeasonRetrieved(null);
            }
        });
    }
}
