package me.jakemoritz.animebuzz.interfaces;

import me.jakemoritz.animebuzz.api.mal.models.UserListHolder;
import me.jakemoritz.animebuzz.api.mal.models.VerifyHolder;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MalEndpointInterface {
    @GET("api/account/verify_credentials.xml")
    Call<VerifyHolder> verifyCredentials();

    @GET("malappinfo.php")
    Call<UserListHolder> getUserList(@Query("u") String username, @Query("status") String status, @Query("type") String type);
}
