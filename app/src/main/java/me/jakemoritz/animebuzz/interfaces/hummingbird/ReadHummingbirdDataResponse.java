package me.jakemoritz.animebuzz.interfaces.hummingbird;

import java.util.List;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.api.ImageRequest;
import me.jakemoritz.animebuzz.models.Series;

public interface ReadHummingbirdDataResponse {
    void hummingbirdSeasonReceived(List<ImageRequest> imageRequests, RealmList<Series> seriesList);
}
