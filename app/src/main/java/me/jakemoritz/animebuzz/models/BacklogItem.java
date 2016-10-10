package me.jakemoritz.animebuzz.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BacklogItem extends RealmObject{

    @PrimaryKey
    private int id;
    private Series series;
    private Long alarmTime;

/*    public BacklogItem(int id, Series series, Long alarmTime) {
        this.id = id;
        this.series = series;
        this.alarmTime = alarmTime;
    }*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Long getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Long alarmTime) {
        this.alarmTime = alarmTime;
    }
}
