package me.jakemoritz.animebuzz.interfaces;

import java.util.ArrayList;

public interface ReadSeasonListResponse {
    void seasonListReceived(ArrayList<SeasonMeta> seasonMetaList);

}
