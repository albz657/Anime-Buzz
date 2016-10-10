package me.jakemoritz.animebuzz.api.mal.models;

public class MatchHolder {

    private String MALID;
    private int episodesWatched;
    private String imageURL;

    public MatchHolder(String MALID, int episodesWatched, String imageURL) {
        this.MALID = MALID;
        this.episodesWatched = episodesWatched;
        this.imageURL = imageURL;
    }

    public String getMALID() {
        return MALID;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public String getImageURL() {
        return imageURL;
    }
}
