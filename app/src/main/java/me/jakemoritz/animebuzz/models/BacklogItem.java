package me.jakemoritz.animebuzz.models;

public class BacklogItem {

    private Series series;
    private Long alarmTime;

    public Series getSeries() {
        return series;
    }

    public Long getAlarmTime() {
        return alarmTime;
    }

    public BacklogItem(Series series, Long alarmTime) {
        this.series = series;
        this.alarmTime = alarmTime;
    }
}
