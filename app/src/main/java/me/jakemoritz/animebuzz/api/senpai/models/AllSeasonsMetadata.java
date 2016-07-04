package me.jakemoritz.animebuzz.api.senpai.models;

import java.util.List;

import me.jakemoritz.animebuzz.models.SeasonMetadata;

public class AllSeasonsMetadata {

    private List<SeasonMetadata> metadataList;
    private String latestSeasonKey;

    public AllSeasonsMetadata(List<SeasonMetadata> metadataList, String latestSeasonKey) {
        this.metadataList = metadataList;
        this.latestSeasonKey = latestSeasonKey;
    }

    public List<SeasonMetadata> getMetadataList() {
        return metadataList;
    }

    public String getLatestSeasonKey() {
        return latestSeasonKey;
    }
}
