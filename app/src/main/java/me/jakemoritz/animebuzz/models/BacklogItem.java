package me.jakemoritz.animebuzz.models;

import com.orm.SugarRecord;

public class BacklogItem extends SugarRecord{

    private Series series;
    private Long alarmTime;
    private Long id;

    public BacklogItem(){

    }

    public BacklogItem(Series series, Long alarmTime) {
        this.series = series;
        this.alarmTime = alarmTime;
    }

    public Series getSeries() {
        return series;
    }

    public Long getAlarmTime() {
        return alarmTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
