package me.jakemoritz.animebuzz.interfaces;

import me.jakemoritz.animebuzz.models.Series;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SenpaiEndpointInterface {
    @GET("http://www.senpai.moe/export.php?type=json&src=raw")
    Call<Series> getLatestSeasonData(@Query("type") String type, @Query("src") String source);
}
