package me.jakemoritz.animebuzz.interfaces;

import me.jakemoritz.animebuzz.models.AllSeasonsMetadata;
import me.jakemoritz.animebuzz.models.Season;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SenpaiEndpointInterface {
    @GET("/export.php")
    Call<Season> getSeasonData(@Query("type") String type, @Query("src") String source);

    @GET("/export.php")
    Call<AllSeasonsMetadata> getSeasonList(@Query("type") String type, @Query("src") String source);
}
