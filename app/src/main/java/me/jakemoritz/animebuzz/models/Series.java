package me.jakemoritz.animebuzz.models;

import android.graphics.Bitmap;

public class Series {

    private int airdate_u;
    private String name;
    private int MALID;
    private boolean currentlyAiring;
    private int simulcast_airdate_u;
    private boolean isInUserList;
    private String season;

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    private Bitmap poster;


    public Bitmap getPoster() {
        return poster;
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

    public int getSimulcast_airdate_u() {
        return simulcast_airdate_u;
    }

    public String getName() {
        return name;
    }

    public int getMALID() {
        return MALID;
    }

    public int getAirdate_u() {
        return airdate_u;
    }

    public Series() {

    }

    public Series(int airdate_u, String name, int MALID, int simulcast_airdate_u, boolean isInUserList, String season, boolean currentlyAiring) {
        this.airdate_u = airdate_u;
        this.name = name;
        this.MALID = MALID;
        this.simulcast_airdate_u = simulcast_airdate_u;
        this.isInUserList = isInUserList;
        this.season = season;
        this.currentlyAiring = currentlyAiring;
    }
}
