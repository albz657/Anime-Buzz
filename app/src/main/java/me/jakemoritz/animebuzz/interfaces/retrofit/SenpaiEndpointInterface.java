package me.jakemoritz.animebuzz.interfaces.retrofit;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.models.Season;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SenpaiEndpointInterface {
    @GET("/export.php")
    Call<String> getSeasonData(@Query("type") String type, @Query("src") String source);

    @GET("/export.php")
    Call<RealmList<Season>> getSeasonList(@Query("type") String type, @Query("src") String source);
}
