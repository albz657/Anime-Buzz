package me.jakemoritz.animebuzz.interfaces;

import me.jakemoritz.animebuzz.api.ann.models.ANNXMLHolder;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ANNEndpointInterface {
    @GET("/encyclopedia/api.xml")
    Call<ANNXMLHolder> getImageUrls(@Query("anime") String anime);
}
