package me.jakemoritz.animebuzz.models;

import java.util.ArrayList;
import java.util.List;

public class Series {

    private int airdate;
    private String name;
    private int MALID;
    private boolean currentlyAiring;
    private String simulcast;
    private int simulcast_airdate;
    private boolean isInUserList;
    private String season;
    private int ANNID;
    private double simulcast_delay;
    private long nextEpisodeAirtime;
    private long nextEpisodeSimulcastTime;
    private List<Long> backlog;
    private int episodesWatched;
    private String nextEpisodeAirtimeFormatted;
    private String nextEpisodeSimulcastTimeFormatted;

    public String getNextEpisodeSimulcastTimeFormatted() {
        return nextEpisodeSimulcastTimeFormatted;
    }

    public void setNextEpisodeSimulcastTimeFormatted(String nextEpisodeSimulcastTimeFormatted) {
        this.nextEpisodeSimulcastTimeFormatted = nextEpisodeSimulcastTimeFormatted;
    }

    public String getNextEpisodeAirtimeFormatted() {
        return nextEpisodeAirtimeFormatted;
    }

    public void setNextEpisodeAirtimeFormatted(String nextEpisodeAirtimeFormatted) {
        this.nextEpisodeAirtimeFormatted = nextEpisodeAirtimeFormatted;
    }

    public void setAirdate(int airdate) {
        this.airdate = airdate;
    }

    public void setCurrentlyAiring(boolean currentlyAiring) {
        this.currentlyAiring = currentlyAiring;
    }

    public void setSimulcast(String simulcast) {
        this.simulcast = simulcast;
    }

    public void setSimulcast_airdate(int simulcast_airdate) {
        this.simulcast_airdate = simulcast_airdate;
    }

    public void setANNID(int ANNID) {
        this.ANNID = ANNID;
    }

    public long getNextEpisodeAirtime() {
        return nextEpisodeAirtime;
    }

    public void setNextEpisodeAirtime(long nextEpisodeAirtime) {
        this.nextEpisodeAirtime = nextEpisodeAirtime;
    }

    public long getNextEpisodeSimulcastTime() {
        return nextEpisodeSimulcastTime;
    }

    public void setNextEpisodeSimulcastTime(long nextEpisodeSimulcastTime) {
        this.nextEpisodeSimulcastTime = nextEpisodeSimulcastTime;
    }

    public double getSimulcast_delay() {
        return simulcast_delay;
    }

    public void setSimulcast_delay(double simulcast_delay) {
        this.simulcast_delay = simulcast_delay;
    }

    public int getANNID() {
        return ANNID;
    }

    public boolean isCurrentlyAiring() {
        return currentlyAiring;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setInUserList(boolean inUserList) {
        isInUserList = inUserList;
    }

    public boolean isInUserList() {
        return isInUserList;
    }

    public int getSimulcast_airdate() {
        return simulcast_airdate;
    }

    public String getName() {
        return name;
    }

    public int getMALID() {
        return MALID;
    }

    public int getAirdate() {
        return airdate;
    }

    public String getSimulcast() {
        return simulcast;
    }

    public List<Long> getBacklog() {
        return backlog;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public void setEpisodesWatched(int episodesWatched) {
        this.episodesWatched = episodesWatched;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Series series = (Series) o;

        return (MALID == series.MALID);
    }

    @Override
    public int hashCode() {
        return MALID;
    }

    public Series(int airdate, String name, int MALID, String simulcast, int simulcast_airdate, String season, int ANNID, double simulcast_delay) {
        this.airdate = airdate;
        this.name = name;
        this.MALID = MALID;
        this.simulcast = simulcast;
        this.simulcast_airdate = simulcast_airdate;
        this.season = season;
        this.ANNID = ANNID;
        this.simulcast_delay = simulcast_delay;
        this.isInUserList = false;
        this.currentlyAiring = false;
        this.backlog = new ArrayList<>();
        this.nextEpisodeAirtime = 0;
        this.nextEpisodeSimulcastTime = 0;
        this.episodesWatched = 0;
        this.nextEpisodeAirtimeFormatted = "";
        this.nextEpisodeSimulcastTimeFormatted = "";
    }

    public Series(int airdate, String name, int MALID, String simulcast, int simulcast_airdate, String season, int ANNID, double simulcast_delay, boolean isInUserList, boolean currentlyAiring, List<Long> backlog, long nextEpisodeAirtime, long nextEpisodeSimulcastTime, int episodesWatched, String nextEpisodeAirtimeFormatted, String nextEpisodeSimulcastTimeFormatted) {
        this.airdate = airdate;
        this.name = name;
        this.MALID = MALID;
        this.simulcast = simulcast;
        this.simulcast_airdate = simulcast_airdate;
        this.season = season;
        this.ANNID = ANNID;
        this.simulcast_delay = simulcast_delay;
        this.isInUserList = isInUserList;
        this.currentlyAiring = currentlyAiring;
        this.backlog = backlog;
        this.nextEpisodeAirtime = nextEpisodeAirtime;
        this.nextEpisodeSimulcastTime = nextEpisodeSimulcastTime;
        this.episodesWatched = episodesWatched;
        this.nextEpisodeSimulcastTimeFormatted = nextEpisodeSimulcastTimeFormatted;
        this.nextEpisodeAirtimeFormatted = nextEpisodeAirtimeFormatted;
    }

}
