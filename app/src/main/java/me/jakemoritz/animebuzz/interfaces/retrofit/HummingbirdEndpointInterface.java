package me.jakemoritz.animebuzz.interfaces.retrofit;

import me.jakemoritz.animebuzz.api.hummingbird.KitsuAnimeHolder;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HummingbirdEndpointInterface {
    @Headers({"Accept: application/vnd.api+json", "Content-Type: application/vnd.api+json"})
    @GET("edge/anime/{ID}")
    Call<KitsuAnimeHolder> getAnimeData(@Path("ID") String ID);

    @Headers({"Accept: application/vnd.api+json", "Content-Type: application/vnd.api+json"})
    @GET("edge/mappings")
    Call<String> getKitsuId(@Query("filter[external_site]") String type, @Query("filter[external_id]") String MALID);
}
