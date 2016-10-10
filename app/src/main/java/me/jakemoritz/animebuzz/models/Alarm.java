package me.jakemoritz.animebuzz.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Alarm extends RealmObject {

    @PrimaryKey
    private String MALID;
    private Series series;
    private long alarmTime;

/*    public Alarm(String MALID, Series series, long alarmTime) {
        this.MALID = MALID;
        this.series = series;
        this.alarmTime = alarmTime;
    }*/

    public String getMALID() {
        return MALID;
    }

    public void setMALID(String MALID) {
        this.MALID = MALID;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public long getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(long alarmTime) {
        this.alarmTime = alarmTime;
    }
}
