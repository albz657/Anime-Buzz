package me.jakemoritz.animebuzz.models;

import com.orm.SugarRecord;

public class AlarmHolder extends SugarRecord{

    private String seriesName;
    private long alarmTime;
    private Long id;
    private int MALID;

    public String getSeriesName() {
        return seriesName;
    }

    public long getAlarmTime() {
        return alarmTime;
    }

    public Long getId() {
        return id;
    }

    public void setAlarmTime(long alarmTime) {
        this.alarmTime = alarmTime;
    }

    public AlarmHolder(){

    }

    public AlarmHolder(String seriesName, long alarmTime, Long id, int MALID) {
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setMALID(int MALID) {
        this.MALID = MALID;
    }

    public int getMALID() {
        return MALID;
    }
}
