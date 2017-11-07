package me.jakemoritz.animebuzz.model;

import com.google.gson.annotations.SerializedName;

public class JikanAnime {

    private String title;

    @SerializedName("title-english")
    private String englishTitle;

    private String synopsis;

    @SerializedName("link-canonical")
    private String malLink;

    @SerializedName("image")
    private String imageUrl;

    private String type;

    private int episodes;

    @SerializedName("aired")
    private String airingDateRange;

    private String malId;

    public JikanAnime(String title, String englishTitle, String synopsis, String malLink, String imageUrl, String type, int episodes, String airingDateRange) {
        this.title = title;
        this.englishTitle = englishTitle;
        this.synopsis = synopsis;
        this.malLink = malLink;
        this.imageUrl = imageUrl;
        this.type = type;
        this.episodes = episodes;
        this.airingDateRange = airingDateRange;
    }

    public String getTitle() {
        return title;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getMalLink() {
        return malLink;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getType() {
        return type;
    }

    public int getEpisodes() {
        return episodes;
    }

    public String getAiringDateRange() {
        return airingDateRange;
    }

    public String getMalId() {
        return malId;
    }

    public void setMalId(String malId) {
        this.malId = malId;
    }
}
