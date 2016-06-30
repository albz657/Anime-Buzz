package me.jakemoritz.animebuzz.interfaces;

import java.util.List;

import me.jakemoritz.animebuzz.models.SeasonMetadata;

public interface ReadSeasonListResponse {
    void seasonListReceived(List<SeasonMetadata> seasonMetaList);

}
