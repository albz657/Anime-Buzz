package me.jakemoritz.animebuzz.api.senpai;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.Realm;
import me.jakemoritz.animebuzz.api.senpai.deserializers.AnimeDeserializer;
import me.jakemoritz.animebuzz.api.senpai.deserializers.SeasonDeserializer;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.fragments.UserListFragment;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.interfaces.retrofit.SenpaiEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.api.senpai.models.SeasonHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SenpaiExportHelper {

    private final static String TAG = SenpaiExportHelper.class.getSimpleName();

    private final static String BASE_URL = "http://www.senpai.moe/";
    private SeriesFragment seriesFragment;

    public SenpaiExportHelper(SeriesFragment seriesFragment) {
        this.seriesFragment = seriesFragment;
    }

    public void getSeasonList() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(String.class, new SeasonDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(App.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<String> call = senpaiEndpointInterface.getSeasonList("json", "seasonlist");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    seriesFragment.senpaiSeasonListReceived();
                } else {
                    if (App.getInstance().isInitializing()){
                        seriesFragment.failedInitialization();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (App.getInstance().isInitializing()){
                    seriesFragment.failedInitialization();
                }
            }
        });
    }

    public void getSeasonData(final String seasonKey) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SeasonHolder.class, new AnimeDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(App.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SenpaiEndpointInterface senpaiEndpointInterface = retrofit.create(SenpaiEndpointInterface.class);
        Call<SeasonHolder> call = senpaiEndpointInterface.getSeasonData("json", seasonKey);
        call.enqueue(new Callback<SeasonHolder>() {
            @Override
            public void onResponse(@NonNull Call<SeasonHolder> call, @NonNull final retrofit2.Response<SeasonHolder> response) {
                if (response.isSuccessful()) {
                    if (seasonKey.equals("raw") && response.body() != null){
                        SharedPrefsUtils.getInstance().setLastUpdateTime(System.currentTimeMillis());
                        SharedPrefsUtils.getInstance().setLatestSeasonKey(response.body().getSeasonKey());
                        SharedPrefsUtils.getInstance().setLatestSeasonName(response.body().getSeasonName());

                        if (seriesFragment instanceof UserListFragment && App.getInstance().isInitializing()){
                            Season currentSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findFirst();
                            seriesFragment.setCurrentlyBrowsingSeason(currentSeason);
                        }
                    }

                    if (App.getInstance().isPostInitializing()){
                        App.getInstance().incrementTotalSyncingSeriesPost(response.body().getSeriesList().size());
                    }

                    App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(response.body().getSeriesList());
                        }
                    });

                    seriesFragment.senpaiSeasonRetrieved(response.body().getSeasonKey());
                } else {
                    seriesFragment.senpaiSeasonRetrieved(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SeasonHolder> call, @NonNull Throwable t) {
                seriesFragment.senpaiSeasonRetrieved(null);
            }
        });
    }
}
