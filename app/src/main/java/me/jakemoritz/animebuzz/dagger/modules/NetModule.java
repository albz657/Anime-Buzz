package me.jakemoritz.animebuzz.dagger.modules;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.jakemoritz.animebuzz.network.MalHeader;
import me.jakemoritz.animebuzz.BuildConfig;
import me.jakemoritz.animebuzz.services.JikanService;
import me.jakemoritz.animebuzz.services.MalService;
import me.jakemoritz.animebuzz.services.SenpaiService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

@Module
public class NetModule {

    private static final String MAL_ENDPOINT = "https://myanimelist.net/api/";
    private static final String JIKAN_ENDPOINT = "http://www.senpai.moe/";

    public NetModule() {
    }

    @Provides
    @Singleton
    Gson provideGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(){
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(loggingInterceptor);
        return builder.build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofitForJikan(Gson gson, OkHttpClient okHttpClient){
        return new Retrofit.Builder()
                .baseUrl(JIKAN_ENDPOINT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    JikanService provideJikanService(Retrofit retrofitForJikan){
        return retrofitForJikan.create(JikanService.class);
    }

    @Provides
    @Singleton
    SenpaiService provideSenpaiService(Retrofit retrofit){
        return retrofit.create(SenpaiService.class);
    }

    @Provides
    @Singleton
    MalHeader provideMalHeader(){
        return MalHeader.getInstance();
    }

    @Provides
    @Singleton
    MalService provideMalService(MalHeader malHeader) {
        String credentials = "Skyrocketing" + ":" + "udI1YQU$f5^E";
        String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", basic)
                    .header("Accept", "application/xml")
                    .method(original.method(), original.body());

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        OkHttpClient client = httpClientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MAL_ENDPOINT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(MalService.class);
    }
}
