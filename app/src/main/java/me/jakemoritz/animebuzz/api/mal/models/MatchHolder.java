package me.jakemoritz.animebuzz.api.mal.models;

public class MatchHolder {

    int MALID;
    int episodesWatched;
    String imageURL;

    public String getImageURL() {
        return imageURL;
    }

    public int getMALID() {
        return MALID;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public MatchHolder(int MALID, int episodesWatched, String imageURL) {
        this.MALID = MALID;
        this.imageURL = imageURL;
        this.episodesWatched = episodesWatched;
    }
}
