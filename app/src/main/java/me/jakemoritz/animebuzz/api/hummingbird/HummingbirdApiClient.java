package me.jakemoritz.animebuzz.api.hummingbird;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import me.jakemoritz.animebuzz.interfaces.retrofit.HummingbirdEndpointInterface;
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
        OkHttpClient client = httpBuilder.build();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HummingbirdAnimeHolder.class, new AnimeDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(serviceClass);
    }

    public void getAnimeData() {
        HummingbirdEndpointInterface hummingbirdEndpointInterface = createService(HummingbirdEndpointInterface.class);
        Call<HummingbirdAnimeHolder> call = hummingbirdEndpointInterface.getAnimeData("21");
        call.enqueue(new Callback<HummingbirdAnimeHolder>() {
            @Override
            public void onResponse(Call<HummingbirdAnimeHolder> call, Response<HummingbirdAnimeHolder> response) {
                Log.d(TAG, "failed");

            }

            @Override
            public void onFailure(Call<HummingbirdAnimeHolder> call, Throwable t) {
                Log.d(TAG, "failed");
            }
        });
    }
}
