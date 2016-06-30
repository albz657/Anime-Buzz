package me.jakemoritz.animebuzz.models;

import java.util.List;

public class Season {

    private List<Series> seasonSeries;
    private SeasonMetadata seasonMetadata;

    public Season(List<Series> seasonSeries, SeasonMetadata seasonMetadata) {
        this.seasonSeries = seasonSeries;
        this.seasonMetadata = seasonMetadata;
    }

    public List<Series> getSeasonSeries() {
        return seasonSeries;
    }

    public SeasonMetadata getSeasonMetadata() {
        return seasonMetadata;
    }
}
