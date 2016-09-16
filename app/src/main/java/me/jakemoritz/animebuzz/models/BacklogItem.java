package me.jakemoritz.animebuzz.models;

public class BacklogItem {

    private Series series;
    private Long alarmTime;
    private int id;

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

    public BacklogItem(Series series, Long alarmTime, int id) {
        this.series = series;
        this.alarmTime = alarmTime;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
