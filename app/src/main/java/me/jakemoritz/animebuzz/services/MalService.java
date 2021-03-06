package me.jakemoritz.animebuzz.services;

import com.oussaki.rxfilesdownloader.FileContainer;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import me.jakemoritz.animebuzz.model.MalUserObject;
import me.jakemoritz.animebuzz.model.MalVerifyCredentialsWrapper;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * This interface defines the endpoints to connect to the MyAnimeList API
 *
 * Documentation: https://myanimelist.net/modules.php?go=api
 */
public interface MalService {

    @GET("api/account/verify_credentials.xml")
    Single<MalVerifyCredentialsWrapper> verifyCredentials();

    @GET("malappinfo.php")
    Single<MalUserObject> getUserAnimeList(@Query("u") String malUsername, @Query("status") String entryStatus, @Query("type") String listType);

    @FormUrlEncoded
    @POST("api/animelist/add/{malId}.xml")
    Completable addAnimeToList(@Path("malId") String malId, @Field("data") String malAnimeValues);

    @FormUrlEncoded
    @POST("api/animelist/update/{malId}.xml")
    Completable updateAnimeInList(@Path("malId") String malId, @Field("data") String malAnimeValues);

    @DELETE("api/animelist/delete/{malId}.xml")
    Completable deleteAnimeFromList(@Path("malId") String malId);

    Single<List<FileContainer>> getUserAvatar();

}
