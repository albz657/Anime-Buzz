package me.jakemoritz.animebuzz.models;

public class AlarmHolder {

    private String seriesName;
    private long alarmTime;

    public String getSeriesName() {
        return seriesName;
    }

    public long getAlarmTime() {
        return alarmTime;
    }

    public AlarmHolder(String seriesName, long alarmTime) {
        this.seriesName = seriesName;
        this.alarmTime = alarmTime;
    }
}
