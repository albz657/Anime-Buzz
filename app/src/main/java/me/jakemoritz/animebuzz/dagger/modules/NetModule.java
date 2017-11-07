package me.jakemoritz.animebuzz.dagger.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.jakemoritz.animebuzz.BuildConfig;
import me.jakemoritz.animebuzz.services.JikanService;
import me.jakemoritz.animebuzz.services.SenpaiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetModule {

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
                .baseUrl("http://www.senpai.moe/")
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
}
