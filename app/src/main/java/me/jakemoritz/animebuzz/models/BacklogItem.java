package me.jakemoritz.animebuzz.models;

public class BacklogItem {

    private Series seriesName;
    private Long alarmTime;

    public Series getSeriesName() {
        return seriesName;
    }

    public Long getAlarmTime() {
        return alarmTime;
    }

    public BacklogItem(Series seriesName, Long alarmTime) {
        this.seriesName = seriesName;
        this.alarmTime = alarmTime;
    }
}
