package me.jakemoritz.animebuzz.interfaces.hummingbird;

import java.util.List;

import me.jakemoritz.animebuzz.api.mal.models.MALImageRequest;
import me.jakemoritz.animebuzz.models.SeriesList;

public interface ReadHummingbirdDataResponse {
    void hummingbirdSeasonReceived(List<MALImageRequest> malImageRequests, SeriesList seriesList);
}
