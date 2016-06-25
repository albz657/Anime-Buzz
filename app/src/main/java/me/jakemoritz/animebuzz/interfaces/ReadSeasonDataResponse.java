package me.jakemoritz.animebuzz.interfaces;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.Series;

public interface ReadSeasonDataResponse {
    void seasonDataRetrieved(ArrayList<Series> seriesList);
}
