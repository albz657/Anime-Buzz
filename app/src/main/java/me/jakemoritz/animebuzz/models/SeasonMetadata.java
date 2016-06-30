package me.jakemoritz.animebuzz.models;

public class SeasonMetadata {

    private String name;
    private String start_timestamp;
    private String key;

    public SeasonMetadata(String name, String start_timestamp, String key) {
        this.name = name;
        this.start_timestamp = start_timestamp;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getStart_timestamp() {
        return start_timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
