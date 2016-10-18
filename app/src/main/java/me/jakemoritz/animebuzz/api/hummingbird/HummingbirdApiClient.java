package me.jakemoritz.animebuzz.api.hummingbird;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.interfaces.retrofit.HummingbirdEndpointInterface;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.tasks.ProcessHBResponseTask;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    private int finishedCount = 0;
    private int listSize = 0;
    private List<HummingbirdAnimeHolder> responses;
    private RealmList<Series> seriesList;

    public HummingbirdApiClient(SeriesFragment callback) {
        this.callback = callback;
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                final Request request = chain.request().newBuilder()
                        .addHeader("X-Client-Id", "683b6ab4486e5a7c612e")
                        .build();

                return chain.proceed(request);
            }
        };
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HummingbirdAnimeHolder.class, new HummingbirdAnimeDeserializer());
        Gson gson = gsonBuilder.create();

        this.retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public void processSeriesList(RealmList<Series> seriesList) {
        if (seriesList.isEmpty()) {
            callback.hummingbirdSeasonReceived();
        } else {
            this.seriesList = seriesList;
            this.responses = new ArrayList<>();
            this.listSize = seriesList.size();

            for (Series series : seriesList) {
                getSeriesData(series.getMALID());
            }
        }
    }

    private void getSeriesData(final String MALID) {
        HummingbirdEndpointInterface hummingbirdEndpointInterface = retrofit.create(HummingbirdEndpointInterface.class);
        Call<HummingbirdAnimeHolder> call = hummingbirdEndpointInterface.getAnimeData(MALID);
        call.enqueue(new Callback<HummingbirdAnimeHolder>() {
            @Override
            public void onResponse(Call<HummingbirdAnimeHolder> call, Response<HummingbirdAnimeHolder> response) {
                if (response.isSuccessful()) {
                    response.body().setMALID(MALID);
                    responses.add(response.body());
                } else {
                    Log.d(TAG, "Failed getting Hummingbird data for '" + MALID + "'");
                }
                finishedCheck();
            }

            @Override
            public void onFailure(Call<HummingbirdAnimeHolder> call, Throwable t) {
                finishedCheck();
                Log.d(TAG, "Failed getting Hummingbird data for '" + "'");
            }
        });
    }

    private void finishedCheck() {
        finishedCount++;
        if (finishedCount == listSize) {
            finishedCount = 0;
            ProcessHBResponseTask task = new ProcessHBResponseTask(callback, seriesList);
            task.execute(responses);
        }
    }
}
