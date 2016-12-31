package me.jakemoritz.animebuzz.api.hummingbird;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.interfaces.retrofit.KitsuEndpointInterface;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.services.KitsuDataProcessor;
import me.jakemoritz.animebuzz.services.PosterDownloader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KitsuApiClient {
    private final static String TAG = KitsuApiClient.class.getSimpleName();

    private static final String BASE_URL = " https://kitsu.io/api/";
    private static final String clientId = "***REMOVED***";
    private static final String clientSecret = "***REMOVED***";
    private SeriesFragment callback;

    public KitsuApiClient(SeriesFragment callback) {
        this.callback = callback;
    }

    public void processSeriesList(String seasonKey) {
        RealmResults<Series> seriesRealmResults = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", seasonKey).findAll();

        if (seriesRealmResults.isEmpty()) {
            callback.hummingbirdSeasonReceived();
        } else {
            if (App.getInstance().isInitializing()){
                App.getInstance().setTotalSyncingSeriesInitial(seriesRealmResults.size());
            }

            for (Series series : seriesRealmResults) {
                getSeriesData(series.getMALID());
            }
            callback.hummingbirdSeasonReceived();
        }
    }

    private void getKitsuID(final String MALID){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(String.class, new KitsuMappingDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(App.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        KitsuEndpointInterface kitsuEndpointInterface = retrofit.create(KitsuEndpointInterface.class);
        Call<String> call = kitsuEndpointInterface.getKitsuId("myanimelist/anime", MALID);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()){
                    final String kitsuId = response.body();

                    App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Series series = realm.where(Series.class).equalTo("MALID", MALID).findFirst();
                            series.setKitsuID(kitsuId);
                        }
                    });

                    executeGetSeriesData(kitsuId, MALID);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void getSeriesData(String MALID) {
        Realm realm = Realm.getDefaultInstance();
        Series currSeries = realm.where(Series.class).equalTo("MALID", MALID).findFirst();
        realm.close();

        if (currSeries.getKitsuID().isEmpty()){
            getKitsuID(MALID);
        } else {
            executeGetSeriesData(currSeries.getKitsuID(), MALID);
        }
    }

    private void executeGetSeriesData(String kitsuId, final String MALID){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(KitsuAnimeHolder.class, new KitsuDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(App.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        KitsuEndpointInterface kitsuEndpointInterface = retrofit.create(KitsuEndpointInterface.class);
        Call<KitsuAnimeHolder> call = kitsuEndpointInterface.getAnimeData(kitsuId);
        call.enqueue(new Callback<KitsuAnimeHolder>() {
            @Override
            public void onResponse(Call<KitsuAnimeHolder> call, Response<KitsuAnimeHolder> response) {
                if (response.isSuccessful()) {
                    Intent hbIntent = new Intent(App.getInstance(), KitsuDataProcessor.class);
                    hbIntent.putExtra("englishTitle", response.body().getEnglishTitle());
                    hbIntent.putExtra("MALID", MALID);
                    hbIntent.putExtra("episodeCount", response.body().getEpisodeCount());
                    hbIntent.putExtra("finishedAiringDate", response.body().getFinishedAiringDate());
                    hbIntent.putExtra("startedAiringDate", response.body().getStartedAiringDate());
                    hbIntent.putExtra("showType", response.body().getShowType());
                    App.getInstance().startService(hbIntent);

                    String imageURL = response.body().getImageURL();
                    File cacheDirectory = App.getInstance().getCacheDir();
                    File bitmapFile = new File(cacheDirectory, MALID + ".jpg");
                    if (!imageURL.isEmpty() && App.getInstance().getResources().getIdentifier("malid_" + MALID, "drawable", "me.jakemoritz.animebuzz") == 0 && !bitmapFile.exists()) {
                        Intent imageIntent = new Intent(App.getInstance(), PosterDownloader.class);
                        imageIntent.putExtra("url", imageURL);
                        imageIntent.putExtra("MALID", MALID);
                        App.getInstance().startService(imageIntent);
                    }
                } else {
                    Log.d(TAG, "Failed getting Hummingbird data for '" + MALID + "'");
                    if (App.getInstance().isPostInitializing()){
                        App.getInstance().incrementCurrentSyncingSeriesPost();
                        NotificationHelper.getInstance().createSeasonDataNotification();
                    }
                }
            }

            @Override
            public void onFailure(Call<KitsuAnimeHolder> call, Throwable t) {
                Log.d(TAG, "Failed getting Hummingbird data for '" + MALID + "'");
                if (App.getInstance().isPostInitializing()){
                    App.getInstance().incrementCurrentSyncingSeriesPost();
                    NotificationHelper.getInstance().createSeasonDataNotification();
                }
            }
        });
    }
}
