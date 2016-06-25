package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Season implements Serializable{

    private String name;
    private String startTimestamp;
    private String key;

    public Season(String startTimestamp, String name, String key) {
        this.startTimestamp = startTimestamp;
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }
}
