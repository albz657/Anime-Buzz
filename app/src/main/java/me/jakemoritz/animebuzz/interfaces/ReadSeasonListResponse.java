package me.jakemoritz.animebuzz.interfaces;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.Season;

public interface ReadSeasonListResponse {
    void seasonListReceived(ArrayList<Season> seasonList);

}
