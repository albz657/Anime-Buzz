package me.jakemoritz.animebuzz.services;

import io.reactivex.Single;
import me.jakemoritz.animebuzz.model.JikanAnime;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface JikanService {

    @GET("anime/{malId}")
    Single<JikanAnime> getAnime(@Path("malId") String malId);
}
