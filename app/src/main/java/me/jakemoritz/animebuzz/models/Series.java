package me.jakemoritz.animebuzz.models;

import java.util.Calendar;

public class Series {

    private int airdate;
    private String title;
    private int mal_id;
    private boolean isSimulcastAired;
    private boolean isAired;
    private Calendar calAirdate;
    private Calendar calSimulcastAidate;

    public Calendar getCalSimulcastAidate() {
        return calSimulcastAidate;
    }

    public void setCalSimulcastAidate(Calendar calSimulcastAidate) {
        this.calSimulcastAidate = calSimulcastAidate;
    }

    public Calendar getCalAirdate() {
        return calAirdate;
    }

    public void setCalAirdate(Calendar calAirdate) {
        this.calAirdate = calAirdate;
    }

    public boolean isCurrentlyAiring() {
        return currentlyAiring;
    }

    private boolean currentlyAiring;
    private int simulcast_airdate;
    private boolean isInUserList;

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    private String season;

    public void setInUserList(boolean inUserList) {
        isInUserList = inUserList;
    }

    public boolean isInUserList() {
        return isInUserList;
    }

    public int getSimulcast_airdate() {
        return simulcast_airdate;
    }

    public String getTitle() {
        return title;
    }

    public int getMal_id() {
        return mal_id;
    }

    public boolean isSimulcastAired() {
        return isSimulcastAired;
    }

    public boolean isAired() {
        return isAired;
    }

    public int getAirdate() {
        return airdate;
    }

    public Series() {
    }

    public Series(int airdate, String title, int mal_id, boolean isSimulcastAired, boolean isAired, int simulcast_airdate, boolean isInUserList, String season, boolean currentlyAiring, Calendar calAirdate, Calendar calSimulcastAidate) {
        this.airdate = airdate;
        this.title = title;
        this.mal_id = mal_id;
        this.isSimulcastAired = isSimulcastAired;
        this.isAired = isAired;
        this.simulcast_airdate = simulcast_airdate;
        this.isInUserList = isInUserList;
        this.season = season;
        this.currentlyAiring = currentlyAiring;
        this.calAirdate = calAirdate;
        this.calSimulcastAidate = calSimulcastAidate;
    }
}
