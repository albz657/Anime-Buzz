package me.jakemoritz.animebuzz.interfaces.retrofit;

import me.jakemoritz.animebuzz.api.hummingbird.HummingbirdAnimeHolder;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface HummingbirdEndpointInterface {
    @GET("api/v2/anime/myanimelist:{MALID}")
    Call<HummingbirdAnimeHolder> getAnimeData(@Path("MALID") String MALID, @Header("X-Client-Id") String clientId);
}
