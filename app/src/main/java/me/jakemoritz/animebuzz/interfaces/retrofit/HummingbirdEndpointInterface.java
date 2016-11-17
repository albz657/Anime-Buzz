package me.jakemoritz.animebuzz.interfaces.retrofit;

import me.jakemoritz.animebuzz.api.hummingbird.HummingbirdAnimeHolder;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface HummingbirdEndpointInterface {
    @Headers({"X-Client-Id: 83b6ab4486e5a7c612e"})
    @GET("api/v2/anime/myanimelist:{MALID}")
    Call<HummingbirdAnimeHolder> getAnimeData(@Path("MALID") String MALID);
}
