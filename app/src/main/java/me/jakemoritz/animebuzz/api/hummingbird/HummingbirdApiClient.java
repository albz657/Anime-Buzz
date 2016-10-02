package me.jakemoritz.animebuzz.api.hummingbird;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.DateFormatHelper;
import me.jakemoritz.animebuzz.interfaces.retrofit.HummingbirdEndpointInterface;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;
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
    private SeriesList seriesList;
    private int finishedCount = 0;
    private List<MALImageRequest> imageRequests;
    private OkHttpClient okHttpClient;
    private Interceptor interceptor;
    private Retrofit retrofit;

    public HummingbirdApiClient(SeriesFragment callback) {
        this.callback = callback;
        this.imageRequests = new ArrayList<>();
        this.interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                final Request request = chain.request().newBuilder()
                        .addHeader("X-Client-Id", "683b6ab4486e5a7c612e")
                        .build();

                return chain.proceed(request);
            }
        };
        this.okHttpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HummingbirdAnimeHolder.class, new AnimeDeserializer());
        Gson gson = gsonBuilder.create();

        this.retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }


    void setSeriesList(SeriesList seriesList) {
        this.seriesList = seriesList;
    }

    private static <S> S createService(Class<S> serviceClass) {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();

        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                final Request request = chain.request().newBuilder()
                        .addHeader("X-Client-Id", "683b6ab4486e5a7c612e")
                        .build();

                return chain.proceed(request);
            }
        };

        httpBuilder.addInterceptor(interceptor);
//        okHttpClient = httpBuilder.build();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HummingbirdAnimeHolder.class, new AnimeDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(serviceClass);
    }

    public void processSeriesList(SeriesList seriesList) {
        this.seriesList = seriesList;
        this.imageRequests = new ArrayList<>();

        if (seriesList.isEmpty()){
            callback.hummingbirdSeasonReceived(imageRequests, seriesList);
        } else {
            for (Series series : seriesList) {
                getAnimeData(series);
            }
        }
    }

    private void finishedCheck() {
        finishedCount++;
        if (finishedCount == seriesList.size()) {
            Series.saveInTx(seriesList);
            callback.hummingbirdSeasonReceived(imageRequests, seriesList);
            finishedCount = 0;
        }
    }

    void getAnimeData(Series series) {
        final Series currSeries = series;

        HummingbirdEndpointInterface hummingbirdEndpointInterface = retrofit.create(HummingbirdEndpointInterface.class);
        Call<HummingbirdAnimeHolder> call = hummingbirdEndpointInterface.getAnimeData(currSeries.getMALID().toString());
        call.enqueue(new Callback<HummingbirdAnimeHolder>() {
            @Override
            public void onResponse(Call<HummingbirdAnimeHolder> call, Response<HummingbirdAnimeHolder> response) {
                if (response.isSuccessful()) {
                    HummingbirdAnimeHolder holder = response.body();

                    currSeries.setEnglishTitle(holder.getEnglishTitle());

                    if (holder.getFinishedAiringDate().isEmpty() && holder.getStartedAiringDate().isEmpty()) {
                        currSeries.setAiringStatus("Not yet aired");
                    } else {
                        Calendar currentCalendar = Calendar.getInstance();
                        Calendar startedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getStartedAiringDate());

                        if (holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                            if (currentCalendar.compareTo(startedCalendar) > 0) {
                                currSeries.setAiringStatus("Airing");
                            } else {
                                currSeries.setAiringStatus("Not yet aired");
                            }
                        } else if (!holder.getFinishedAiringDate().isEmpty() && !holder.getStartedAiringDate().isEmpty()) {
                            Calendar finishedCalendar = DateFormatHelper.getInstance().getCalFromHB(holder.getFinishedAiringDate());

                            if (currentCalendar.compareTo(finishedCalendar) > 0) {
                                currSeries.setAiringStatus("Finished airing");
                            }
                        }
                    }

                    if (!holder.getImageURL().isEmpty()) {
                        MALImageRequest malImageRequest = new MALImageRequest(currSeries);
                        malImageRequest.setURL(holder.getImageURL());
                        imageRequests.add(malImageRequest);
                    }

//                    currSeries.save();
                } else {
                    Log.d(TAG, "Failed getting Hummingbird data for '" + currSeries.getName() + "'");
                }
                finishedCheck();
            }

            @Override
            public void onFailure(Call<HummingbirdAnimeHolder> call, Throwable t) {
                finishedCheck();

                Log.d(TAG, "Failed getting Hummingbird data for '" + currSeries.getName() + "'");
            }
        });
    }
}
