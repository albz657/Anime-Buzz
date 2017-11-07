package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.dagger.modules.MalVerifyCredentialsWrapper;
import retrofit2.http.GET;

public interface MalService {

    @GET("account/verify_credentials.xml")
    Single<MalVerifyCredentialsWrapper> verifyCredentials();

}
