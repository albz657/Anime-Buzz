package me.jakemoritz.animebuzz.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.AlarmHolder;

public class AlarmsDataHelper {

    private static final String TAG = AlarmsDataHelper.class.getSimpleName();

    private static String TABLE_ALARMS = "TABLE_ALARMS";

    // Alarm columns
    private static final String KEY_ALARM_ID = "alarmid";
    private static final String KEY_ALARM_NAME = "alarmname";
    private static final String KEY_ALARM_TIME = "alarmtime";

    private static AlarmsDataHelper mInstance;

    public static synchronized AlarmsDataHelper getInstance() {
        if (mInstance == null) {
            mInstance = new AlarmsDataHelper();
        }
        return mInstance;
    }

    // retrieval

    public List<AlarmHolder> getAllAlarms(SQLiteDatabase database) {
        List<AlarmHolder> alarms = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ALARMS, null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            AlarmHolder tempAlarm = getAlarmWithCursor(cursor);
            alarms.add(tempAlarm);
            cursor.moveToNext();
        }

        cursor.close();

        return alarms;
    }

    private AlarmHolder getAlarmWithCursor(Cursor res) {
        Long id = res.getLong(res.getColumnIndex(KEY_ALARM_ID));
        long time = res.getLong(res.getColumnIndex(KEY_ALARM_TIME));
        String name = res.getString(res.getColumnIndex(KEY_ALARM_NAME));

        return new AlarmHolder(name, time, id, -1);
    }



    // misc

    void upgradeAlarms(SQLiteDatabase database) {
        List<AlarmHolder> oldAlarms = getAllAlarms(database);

        for (AlarmHolder alarmHolder : oldAlarms) {
            alarmHolder.setMALID(alarmHolder.getId().intValue());
            alarmHolder.save();
        }

        AlarmHelper.getInstance().cancelAllAlarms(oldAlarms);

        List<AlarmHolder> upgradedAlarms = AlarmHolder.listAll(AlarmHolder.class);


        App.getInstance().setAlarms(upgradedAlarms);

        AlarmHelper.getInstance().setAlarmsOnBoot();
    }
}
