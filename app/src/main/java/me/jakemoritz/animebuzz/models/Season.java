package me.jakemoritz.animebuzz.models;

public class Season {
    @Override
    public boolean equals(Object o) {
        return seasonMetadata.equals(o);
    }

    @Override
    public int hashCode() {
        int result = seasonSeries.hashCode();
        result = 31 * result + seasonMetadata.hashCode();
        return result;
    }

    private SeriesList seasonSeries;
    private SeasonMetadata seasonMetadata;

    public Season(SeriesList seasonSeries, SeasonMetadata seasonMetadata) {
        this.seasonSeries = seasonSeries;
        this.seasonMetadata = seasonMetadata;
    }

    public SeriesList getSeasonSeries() {
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
