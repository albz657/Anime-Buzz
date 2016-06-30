package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class SeasonMeta implements Serializable{

    private String name;
    private String start_timestamp;
    private String key;

    public SeasonMeta(String start_timestamp, String name, String key) {
        this.start_timestamp = start_timestamp;
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getStart_timestamp() {
        return start_timestamp;
    }
}
