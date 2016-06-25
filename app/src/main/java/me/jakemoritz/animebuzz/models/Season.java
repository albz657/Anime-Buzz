package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Season implements Serializable{

    private String name;

    public String getKey() {
        return key;
    }

    private String key;

    public Season(Long startTimestamp, String name, String key) {
        this.startTimestamp = startTimestamp;
        this.name = name;
        this.key = key;
    }

    private Long startTimestamp;

    public String getName() {
        return name;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }
}
