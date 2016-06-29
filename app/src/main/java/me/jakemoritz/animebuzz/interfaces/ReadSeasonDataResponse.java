package me.jakemoritz.animebuzz.interfaces;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.SeriesOld;

public interface ReadSeasonDataResponse {
    void seasonDataRetrieved(ArrayList<SeriesOld> seriesList);
}
