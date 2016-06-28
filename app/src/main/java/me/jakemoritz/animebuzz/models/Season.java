package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Season implements Serializable{

    private String name;
/*
    @Override
    public boolean equals(Object obj) {
        return getKey().equals(((Season) obj).getKey());
    }*/

    private String start_timestamp;
    private String key;

    public Season(String start_timestamp, String name, String key) {
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
