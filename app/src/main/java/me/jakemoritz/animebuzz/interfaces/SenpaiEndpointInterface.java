package me.jakemoritz.animebuzz.interfaces;

import me.jakemoritz.animebuzz.models.Season;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SenpaiEndpointInterface {
    @GET("/export.php")
    Call<Season> getLatestSeasonData(@Query("type") String type, @Query("src") String source);
}
