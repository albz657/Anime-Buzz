package me.jakemoritz.animebuzz.models;

import com.orm.SugarRecord;

public class SeasonMetadata extends SugarRecord{

    private String name;
    private String start_timestamp;
    private String key;
    private boolean currentOrNewer;
    private boolean latest;

    public SeasonMetadata(){

    }

    public SeasonMetadata(String name, String start_timestamp, String key) {
        this.name = name;
        this.start_timestamp = start_timestamp;
        this.key = key;
        this.latest = false;
    }

    public SeasonMetadata(String name, String start_timestamp, String key, boolean latest) {
        this.name = name;
        this.start_timestamp = start_timestamp;
        this.key = key;
        this.latest = latest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeasonMetadata metadata = (SeasonMetadata) o;

        return key.equals(metadata.key);
    }

    public boolean isCurrentOrNewer() {
        return currentOrNewer;
    }

    public void setCurrentOrNewer(boolean currentOrNewer) {
        this.currentOrNewer = currentOrNewer;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
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
