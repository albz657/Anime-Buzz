package me.jakemoritz.animebuzz.interfaces;

import me.jakemoritz.animebuzz.xml_holders.MAL.VerifyHolder;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MalEndpointInterface {
    @GET("account/verify_credentials.xml")
    Call<VerifyHolder> verifyCredentials();
}
