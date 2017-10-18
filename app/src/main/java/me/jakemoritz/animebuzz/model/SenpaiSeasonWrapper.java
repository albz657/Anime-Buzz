package me.jakemoritz.animebuzz.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SenpaiSeasonWrapper {

    @SerializedName("meta")
    private SenpaiSeason senpaiSeason;

    @SerializedName("items")
    private List<SenpaiAnime> senpaiAnimeList;

    public SenpaiSeasonWrapper(SenpaiSeason senpaiSeason, List<SenpaiAnime> senpaiAnimeList) {
        this.senpaiSeason = senpaiSeason;
        this.senpaiAnimeList = senpaiAnimeList;
    }

    public SenpaiSeason getSenpaiSeason() {
        return senpaiSeason;
    }

    public List<SenpaiAnime> getSenpaiAnimeList() {
        return senpaiAnimeList;
    }
}
