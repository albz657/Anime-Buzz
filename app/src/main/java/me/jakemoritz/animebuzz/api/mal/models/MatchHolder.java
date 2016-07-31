package me.jakemoritz.animebuzz.api.mal.models;

public class MatchHolder {

    int MALID;
    int episodesWatched;

    public int getMALID() {
        return MALID;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public MatchHolder(int MALID, int episodesWatched) {
        this.MALID = MALID;
        this.episodesWatched = episodesWatched;
    }
}
