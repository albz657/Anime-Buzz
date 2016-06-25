package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Season implements Serializable{

    private String name;

    public String getKey() {
        return key;
    }

    private String key;

    public Season(String startTimestamp, String name, String key) {
        this.startTimestamp = startTimestamp;
        this.name = name;
        this.key = key;
    }

    private String startTimestamp;

    public String getName() {
        return name;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }
}
