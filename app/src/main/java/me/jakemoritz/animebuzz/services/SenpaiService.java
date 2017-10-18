package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.model.SenpaiSeasonWrapper;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SenpaiService {

    @GET("/export.php")
    Single<SenpaiSeasonWrapper> getSeason(@Query("type") String responseFormat, @Query("src") String season);

}
