package me.jakemoritz.animebuzz.api.senpai.models;

import io.realm.RealmList;
import me.jakemoritz.animebuzz.models.Season;

public class AllSeasonsMetadata {

    private RealmList<Season> metadataList;
    private String latestSeasonKey;

    public AllSeasonsMetadata(RealmList<Season> metadataList, String latestSeasonKey) {
        this.metadataList = metadataList;
        this.latestSeasonKey = latestSeasonKey;
    }

    public RealmList<Season> getMetadataList() {
        return metadataList;
    }

    public String getLatestSeasonKey() {
        return latestSeasonKey;
    }
}
