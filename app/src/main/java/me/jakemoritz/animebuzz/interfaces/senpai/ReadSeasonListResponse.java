package me.jakemoritz.animebuzz.interfaces.senpai;

import java.util.List;

import me.jakemoritz.animebuzz.models.SeasonMetadata;

public interface ReadSeasonListResponse {
    void seasonListReceived(List<SeasonMetadata> seasonMetaList);

}
