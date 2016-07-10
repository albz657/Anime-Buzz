package me.jakemoritz.animebuzz.models;

public class BacklogItem {

    private Series series;
    private Long episodeTime;

    public Series getSeries() {
        return series;
    }

    public Long getEpisodeTime() {
        return episodeTime;
    }

    public BacklogItem(Series series, Long episodeTime) {
        this.series = series;
        this.episodeTime = episodeTime;
    }
}
