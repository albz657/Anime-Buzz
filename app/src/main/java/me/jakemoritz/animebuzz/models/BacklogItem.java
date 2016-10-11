package me.jakemoritz.animebuzz.models;

import io.realm.RealmObject;

public class BacklogItem extends RealmObject{

    private Series series;
    private Long alarmTime;

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
