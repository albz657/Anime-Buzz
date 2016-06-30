package me.jakemoritz.animebuzz.interfaces;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.SeasonMeta;

public interface ReadSeasonListResponse {
    void seasonListReceived(ArrayList<SeasonMeta> seasonMetaList);

}
