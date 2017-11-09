package me.jakemoritz.animebuzz.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="anime", strict=false)
public class MalUserAnime {

    @Element(name="series_animedb_id")
    private String MALID;

    @Element(name="my_status")
    private int entryWatchingStatus;

    @Element(name="my_watched_episodes")
    private int watchedEpisodeCount;

    public MalUserAnime(String MALID) {
        this.MALID = MALID;
    }

    public String getMALID() {
        return MALID;
    }

    public int getEntryWatchingStatus() {
        return entryWatchingStatus;
    }

    public int getWatchedEpisodeCount() {
        return watchedEpisodeCount;
    }
}
