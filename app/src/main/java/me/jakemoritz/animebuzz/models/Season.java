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
    private String startDate;

/*    public Season(String key, String name, String startDate) {
        this.key = key;
        this.name = name;
        this.startDate = startDate;
    }

    public Season(String key, String name, RealmList<Series> seasonSeries, int chronologicalIndex, String startDate) {
        this.key = key;
        this.name = name;
        this.seasonSeries = seasonSeries;
        this.chronologicalIndex = chronologicalIndex;
        this.startDate = startDate;
    }*/

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}

