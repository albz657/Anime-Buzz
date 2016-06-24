package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Series implements Serializable{

    private int airdate;
    private String title;
    private int mal_id;
    private boolean isSimulcastAired;
    private boolean isAired;
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

    public Series(int airdate, String title, int mal_id, boolean isSimulcastAired, boolean isAired, int simulcast_airdate, boolean isInUserList, String season) {
        this.airdate = airdate;
        this.title = title;
        this.mal_id = mal_id;
        this.isSimulcastAired = isSimulcastAired;
        this.isAired = isAired;
        this.simulcast_airdate = simulcast_airdate;
        this.isInUserList = isInUserList;
        this.season = season;
    }
}
