package me.jakemoritz.animebuzz.model;

import com.google.gson.annotations.SerializedName;

public class SenpaiSeason {

    private String season;

    @SerializedName("start_u")
    private String startTimestamp;

    public SenpaiSeason(String season, String startTimestamp) {
        this.season = season;
        this.startTimestamp = startTimestamp;
    }

    public String getSeason() {
        return season;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }
}
