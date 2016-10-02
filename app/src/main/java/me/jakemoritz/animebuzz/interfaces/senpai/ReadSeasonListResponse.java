package me.jakemoritz.animebuzz.interfaces.senpai;

import java.util.List;

import me.jakemoritz.animebuzz.models.SeasonMetadata;

public interface ReadSeasonListResponse {
    void senpaiSeasonListReceived(List<SeasonMetadata> seasonMetaList);

}
