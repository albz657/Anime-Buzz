package me.jakemoritz.animebuzz.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Season extends RealmObject{

    @PrimaryKey
    private String key;
    private String name;
    private RealmList<Series> seasonSeries;
    private int chronologicalIndex;

    public Season(String name, String key, int chronologicalIndex) {
        this.name = name;
        this.key = key;
        this.chronologicalIndex = chronologicalIndex;
    }

    public Season(int chronologicalIndex, String key, String name, RealmList<Series> seasonSeries) {
        this.chronologicalIndex = chronologicalIndex;
        this.key = key;
        this.name = name;
        this.seasonSeries = seasonSeries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Season season = (Season) o;

        return key.equals(season.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public int getChronologicalIndex() {
        return chronologicalIndex;
    }

    public void setChronologicalIndex(int chronologicalIndex) {
        this.chronologicalIndex = chronologicalIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Series> getSeasonSeries() {
        return seasonSeries;
    }

    public void setSeasonSeries(RealmList<Series> seasonSeries) {
        this.seasonSeries = seasonSeries;
    }
}

