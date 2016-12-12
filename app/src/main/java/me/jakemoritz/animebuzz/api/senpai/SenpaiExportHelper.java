package me.jakemoritz.animebuzz.api.senpai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.Realm;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.fragments.UserListFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.retrofit.SenpaiEndpointInterface;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.gson.SeasonHolder;
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
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    seriesFragment.senpaiSeasonListReceived();
                } else {
                    if (App.getInstance().isInitializing()){
                        seriesFragment.failedInitialization();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
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
            public void onResponse(Call<SeasonHolder> call, final retrofit2.Response<SeasonHolder> response) {
                if (response.isSuccessful()) {
                    if (seasonKey.equals("raw")){
                        SharedPrefsHelper.getInstance().setLastUpdateTime(System.currentTimeMillis());
                        SharedPrefsHelper.getInstance().setLatestSeasonKey(response.body().getSeasonKey());
                        SharedPrefsHelper.getInstance().setLatestSeasonName(response.body().getSeasonName());

                        if (seriesFragment instanceof UserListFragment && App.getInstance().isInitializing()){
                            Season currentSeason = App.getInstance().getRealm().where(Season.class).equalTo("key", SharedPrefsHelper.getInstance().getLatestSeasonKey()).findFirst();
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
            public void onFailure(Call<SeasonHolder> call, Throwable t) {
                seriesFragment.senpaiSeasonRetrieved(null);
            }
        });
    }
}
