package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Series implements Serializable{

    private String title;
    private int mal_id;
    private boolean isSimulcastAired;
    private boolean isAired;
    private int airdate;

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

    private int simulcast_airdate;


    public Series(int airdate, String title, int mal_id, boolean isSimulcastAired, boolean isAired, int simulcast_airdate) {
        this.airdate = airdate;
        this.title = title;
        this.mal_id = mal_id;
        this.isSimulcastAired = isSimulcastAired;
        this.isAired = isAired;
        this.simulcast_airdate = simulcast_airdate;
    }
}
