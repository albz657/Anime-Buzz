package me.jakemoritz.animebuzz.models;

import java.util.ArrayList;
import java.util.Iterator;
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

    public void removeFromBacklog(long time) {
        for (Iterator backlogIterator = backlog.iterator(); backlogIterator.hasNext();) {
            long episodeTime = (long) backlogIterator.next();
            if (episodeTime == time) {
                backlog.remove(episodeTime);
            }
        }
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
    }

    public Series(int airdate, String name, int MALID, String simulcast, int simulcast_airdate, String season, int ANNID, double simulcast_delay, boolean isInUserList, boolean currentlyAiring, List<Long> backlog, long nextEpisodeAirtime, long nextEpisodeSimulcastTime) {
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
    }

}
