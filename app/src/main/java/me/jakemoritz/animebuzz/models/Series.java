package me.jakemoritz.animebuzz.models;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;

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
    private String nextEpisodeAirtimeFormatted24;
    private String nextEpisodeSimulcastTimeFormatted24;

    public String getNextEpisodeSimulcastTimeFormatted24() {
        return nextEpisodeSimulcastTimeFormatted24;
    }

    public void setNextEpisodeSimulcastTimeFormatted24(String nextEpisodeSimulcastTimeFormatted24) {
        this.nextEpisodeSimulcastTimeFormatted24 = nextEpisodeSimulcastTimeFormatted24;
    }

    public String getNextEpisodeAirtimeFormatted24() {
        return nextEpisodeAirtimeFormatted24;
    }

    public void setNextEpisodeAirtimeFormatted24(String nextEpisodeAirtimeFormatted24) {
        this.nextEpisodeAirtimeFormatted24 = nextEpisodeAirtimeFormatted24;
    }

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

    public String getNextEpisodeTimeFormatted(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        boolean prefers24Hour = sharedPref.getBoolean(App.getInstance().getString(R.string.pref_24hour_key), false);
        boolean prefersSimulcast = sharedPref.getBoolean(App.getInstance().getString(R.string.pref_simulcast_key), false);

        if (prefersSimulcast) {
            if (prefers24Hour){
                return nextEpisodeSimulcastTimeFormatted24;
            } else {
                return nextEpisodeSimulcastTimeFormatted;
            }
        } else {
            if (prefers24Hour){
                return nextEpisodeAirtimeFormatted24;
            } else {
                return nextEpisodeAirtimeFormatted;
            }
        }
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
        this.nextEpisodeAirtimeFormatted24 = "";
        this.nextEpisodeSimulcastTimeFormatted24 = "";
    }

    public Series(int airdate, String name, int MALID, String simulcast, int simulcast_airdate, String season, int ANNID, double simulcast_delay, boolean isInUserList, boolean currentlyAiring, List<Long> backlog, long nextEpisodeAirtime, long nextEpisodeSimulcastTime, int episodesWatched, String nextEpisodeAirtimeFormatted, String nextEpisodeSimulcastTimeFormatted, String nextEpisodeAirtimeFormatted24, String nextEpisodeSimulcastTimeFormatted24) {
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
        this.nextEpisodeSimulcastTimeFormatted24 = nextEpisodeSimulcastTimeFormatted24;
        this.nextEpisodeAirtimeFormatted24 = nextEpisodeAirtimeFormatted24;
    }

}
