package me.jakemoritz.animebuzz.dagger.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.jakemoritz.animebuzz.BuildConfig;
import me.jakemoritz.animebuzz.network.MalHeader;
import me.jakemoritz.animebuzz.services.JikanService;
import me.jakemoritz.animebuzz.services.MalService;
import me.jakemoritz.animebuzz.services.SenpaiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

@Module
public class NetModule {

    private static final String MAL_ENDPOINT = "https://myanimelist.net/";
    private static final String JIKAN_ENDPOINT = "https://jikan.me/api/";
    private static final String SENPAI_ENDPOINT = "http://www.senpai.moe/";

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
        loggingInterceptor.setLevel(BuildConfig.DEBUG ?
                HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(loggingInterceptor);
        return builder.build();
    }

    @Provides
    @Singleton
    @Named("retrofitForJikan")
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
    @Named("retrofitForSenpai")
    Retrofit provideRetrofitForSenpai(Gson gson, OkHttpClient okHttpClient){
        return new Retrofit.Builder()
                .baseUrl(SENPAI_ENDPOINT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    JikanService provideJikanService(@Named("retrofitForJikan") Retrofit retrofitForJikan){
        return retrofitForJikan.create(JikanService.class);
    }

    @Provides
    @Singleton
    SenpaiService provideSenpaiService(@Named("retrofitForSenpai") Retrofit retrofitForSenpai){
        return retrofitForSenpai.create(SenpaiService.class);
    }

    @Provides
    @Singleton
    MalHeader provideMalHeader(){
        return MalHeader.getInstance();
    }

    @Provides
    @Singleton
    MalService provideMalService() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ?
                HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);
        httpClientBuilder.addInterceptor(MalHeader.getInstance());
        httpClientBuilder.addInterceptor(loggingInterceptor);
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
