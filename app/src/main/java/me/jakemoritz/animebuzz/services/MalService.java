package me.jakemoritz.animebuzz.services;

import com.oussaki.rxfilesdownloader.FileContainer;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import me.jakemoritz.animebuzz.model.MalAnimeValues;
import me.jakemoritz.animebuzz.model.MalUserObject;
import me.jakemoritz.animebuzz.model.MalVerifyCredentialsWrapper;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MalService {

    @GET("api/account/verify_credentials.xml")
    Single<MalVerifyCredentialsWrapper> verifyCredentials();

    @GET("malappinfo.php")
    Single<MalUserObject> getUserAnimeList(@Query("u") String malUsername, @Query("status") String entryStatus, @Query("type") String listType);

    @FormUrlEncoded
    @POST("api/animelist/add/{malId}.xml")
    Completable addAnimeToList(@Path("malId") String malId, @Field("data") MalAnimeValues malAnimeValues);

    Single<List<FileContainer>> getUserAvatar();

}
