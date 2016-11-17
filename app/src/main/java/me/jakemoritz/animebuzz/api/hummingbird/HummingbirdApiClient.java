package me.jakemoritz.animebuzz.api.hummingbird;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import io.realm.RealmResults;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.retrofit.HummingbirdEndpointInterface;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.services.HummingbirdDataProcessor;
import me.jakemoritz.animebuzz.services.PosterDownloader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HummingbirdApiClient {
    private final static String TAG = HummingbirdApiClient.class.getSimpleName();

    private static final String BASE_URL = "https://hummingbird.me/";
    private SeriesFragment callback;
    private Retrofit retrofit;

    public HummingbirdApiClient(SeriesFragment callback) {
        this.callback = callback;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HummingbirdAnimeHolder.class, new HummingbirdAnimeDeserializer());
        Gson gson = gsonBuilder.create();

        this.retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(App.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public void processSeriesList(String seasonKey) {
        RealmResults<Series> seriesRealmResults = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", seasonKey).findAll();

        if (seriesRealmResults.isEmpty()) {
            callback.hummingbirdSeasonReceived();
        } else {
            if (App.getInstance().isInitializing()){
                App.getInstance().setTotalSyncingSeries(seriesRealmResults.size());
            }

            for (Series series : seriesRealmResults) {
                getSeriesData(series.getMALID());
            }
            callback.hummingbirdSeasonReceived();
        }
    }

    private void getSeriesData(final String MALID) {
        HummingbirdEndpointInterface hummingbirdEndpointInterface = retrofit.create(HummingbirdEndpointInterface.class);
        Call<HummingbirdAnimeHolder> call = hummingbirdEndpointInterface.getAnimeData(MALID);
        call.enqueue(new Callback<HummingbirdAnimeHolder>() {
            @Override
            public void onResponse(Call<HummingbirdAnimeHolder> call, Response<HummingbirdAnimeHolder> response) {
                if (response.isSuccessful()) {
                    Intent hbIntent = new Intent(App.getInstance(), HummingbirdDataProcessor.class);
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
                }
            }

            @Override
            public void onFailure(Call<HummingbirdAnimeHolder> call, Throwable t) {
                Log.d(TAG, "Failed getting Hummingbird data for '" + MALID + "'");
            }
        });
    }
}
