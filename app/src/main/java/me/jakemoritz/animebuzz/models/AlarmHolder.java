package me.jakemoritz.animebuzz.models;

public class AlarmHolder {

    private String seriesName;
    private long alarmTime;
    private int id;

    public String getSeriesName() {
        return seriesName;
    }

    public long getAlarmTime() {
        return alarmTime;
    }

    public int getId() {
        return id;
    }


    public AlarmHolder(String seriesName, long alarmTime, int id) {
        this.seriesName = seriesName;
        this.alarmTime = alarmTime;
        this.id = id;
    }
}
