package me.jakemoritz.animebuzz.services;

import com.oussaki.rxfilesdownloader.FileContainer;

import java.util.List;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.model.MalUserObject;
import me.jakemoritz.animebuzz.model.MalVerifyCredentialsWrapper;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MalService {

    @GET("api/account/verify_credentials.xml")
    Single<MalVerifyCredentialsWrapper> verifyCredentials();

    @GET("malappinfo.php")
    Single<MalUserObject> getUserAnimeList(@Query("u") String malUsername, @Query("status") String entryStatus, @Query("type") String listType);

    Single<List<FileContainer>> getUserAvatar();

}
