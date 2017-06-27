package me.jakemoritz.animebuzz.interfaces.retrofit;

import me.jakemoritz.animebuzz.api.kitsu.models.KitsuAnimeHolder;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface KitsuEndpointInterface {
    @Headers({"Accept: application/vnd.api+json", "Content-Type: application/vnd.api+json"})
    @GET("edge/anime/{ID}")
    Call<KitsuAnimeHolder> getAnimeData(@Path("ID") String ID);

    @Headers({"Accept: application/vnd.api+json", "Content-Type: application/vnd.api+json"})
    @GET("edge/mappings")
    Call<String> getKitsuMapping(@Query("filter[external_site]") String type, @Query("filter[external_id]") String MALID);

    @Headers({"Accept: application/vnd.api+json", "Content-Type: application/vnd.api+json"})
    @GET("edge/mappings/{MAPPING_ID}/media")
    Call<String> getKitsuId(@Path("MAPPING_ID") String kitsuMapping);
}
