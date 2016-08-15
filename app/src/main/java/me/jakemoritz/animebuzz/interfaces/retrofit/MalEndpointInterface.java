package me.jakemoritz.animebuzz.interfaces.retrofit;

import me.jakemoritz.animebuzz.api.mal.models.UserListHolder;
import me.jakemoritz.animebuzz.api.mal.models.VerifyHolder;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MalEndpointInterface {
    @GET("api/account/verify_credentials.xml")
    Call<VerifyHolder> verifyCredentials();

    @GET("malappinfo.php")
    Call<UserListHolder> getUserList(@Query("u") String username, @Query("status") String status, @Query("type") String type);

    @FormUrlEncoded
    @POST("api/animelist/add/{id}.xml")
    Call<Void> addAnimeURLEncoded(@Field("data") String body, @Path("id") String id);

    @FormUrlEncoded
    @POST("api/animelist/update/{id}.xml")
    Call<Void> updateAnimeEpisodeCount(@Field("data") String body, @Path("id") String id);

    @DELETE("api/animelist/delete/{id}.xml")
    Call<Void> deleteAnime(@Path("id") String id);
}
