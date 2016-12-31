package me.jakemoritz.animebuzz.api.hummingbird;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TokenService {
    @FormUrlEncoded
    @POST("/token")
    AccessToken getAccessToken(
            @Field("code") String code,
            @Field("grant_type") String grantType
    );
}
