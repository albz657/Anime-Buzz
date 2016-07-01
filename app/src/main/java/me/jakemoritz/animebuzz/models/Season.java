package me.jakemoritz.animebuzz.models;

import java.util.List;

public class Season {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Season season = (Season) o;

        if (!seasonSeries.equals(season.seasonSeries)) return false;
        return seasonMetadata.equals(season.seasonMetadata);

    }

    @Override
    public int hashCode() {
        int result = seasonSeries.hashCode();
        result = 31 * result + seasonMetadata.hashCode();
        return result;
    }

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

    @Override
    public String toString() {
        return getSeasonMetadata().getKey();
    }
}
