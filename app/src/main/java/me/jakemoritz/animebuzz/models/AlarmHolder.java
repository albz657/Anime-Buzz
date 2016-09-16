package me.jakemoritz.animebuzz.models;

public class AlarmHolder {

    private String seriesName;
    private long alarmTime;
    private int id;
    private int MALID;

    public String getSeriesName() {
        return seriesName;
    }

    public long getAlarmTime() {
        return alarmTime;
    }

    public int getId() {
        return id;
    }

    public void setAlarmTime(long alarmTime) {
        this.alarmTime = alarmTime;
    }

    public AlarmHolder(String seriesName, long alarmTime, int id, int MALID) {
        this.seriesName = seriesName;
        this.alarmTime = alarmTime;
        this.id = id;
        this.MALID = MALID;
    }

    public AlarmHolder(String seriesName, long alarmTime, int MALID) {
        this.seriesName = seriesName;
        this.alarmTime = alarmTime;
        this.MALID = MALID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMALID(int MALID) {
        this.MALID = MALID;
    }

    public int getMALID() {
        return MALID;
    }
}
