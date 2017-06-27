package me.jakemoritz.animebuzz.interfaces.retrofit;

import me.jakemoritz.animebuzz.api.senpai.models.SeasonHolder;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SenpaiEndpointInterface {
    @GET("/export.php")
    Call<SeasonHolder> getSeasonData(@Query("type") String type, @Query("src") String source);

    @GET("/export.php")
    Call<String> getSeasonList(@Query("type") String type, @Query("src") String source);
}
