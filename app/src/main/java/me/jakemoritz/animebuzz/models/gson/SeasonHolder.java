package me.jakemoritz.animebuzz.models.gson;

import java.util.List;

import me.jakemoritz.animebuzz.models.Series;

public class SeasonHolder {

    private String seasonKey;
    private String seasonName;
    private List<Series> seriesList;

    public SeasonHolder(String seasonKey) {
        this.seasonKey = seasonKey;
    }

    public String getSeasonKey() {
        return seasonKey;
    }

    public void setSeasonKey(String seasonKey) {
        this.seasonKey = seasonKey;
    }

    public List<Series> getSeriesList() {
        return seriesList;
    }

    public void setSeriesList(List<Series> seriesList) {
        this.seriesList = seriesList;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }
}
