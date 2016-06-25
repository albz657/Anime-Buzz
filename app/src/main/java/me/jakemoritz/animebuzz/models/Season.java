package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Season implements Serializable{

    private String name;
    private String startTimestamp;

    public String getName() {
        return name;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public Season(String name, String startTimestamp) {
        this.name = name;
        this.startTimestamp = startTimestamp;
    }
}
