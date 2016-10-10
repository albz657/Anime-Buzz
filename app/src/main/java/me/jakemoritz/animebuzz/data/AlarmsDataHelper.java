package me.jakemoritz.animebuzz.data;

import android.database.sqlite.SQLiteDatabase;

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

/*
    public List<Alarm> getAllAlarms(SQLiteDatabase database) {
        List<Alarm> alarms = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ALARMS, null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Alarm tempAlarm = getAlarmWithCursor(cursor);
            alarms.add(tempAlarm);
            cursor.moveToNext();
        }

        cursor.close();

        return alarms;
    }

    private Alarm getAlarmWithCursor(Cursor res) {
        Long id = res.getLong(res.getColumnIndex(KEY_ALARM_ID));
        long time = res.getLong(res.getColumnIndex(KEY_ALARM_TIME));
        String name = res.getString(res.getColumnIndex(KEY_ALARM_NAME));

        return new Alarm(name, time, id, -1);
    }
*/



    // misc

    void upgradeAlarms(SQLiteDatabase database) {
      /*  List<Alarm> oldAlarms = getAllAlarms(database);

        for (Alarm alarm : oldAlarms) {
            alarm.setMALID(alarm.getId().intValue());
            alarm.save();
        }

        AlarmHelper.getInstance().cancelAllAlarms(oldAlarms);

        List<Alarm> upgradedAlarms = Alarm.listAll(Alarm.class);

        App.getInstance().setAlarms(upgradedAlarms);

        AlarmHelper.getInstance().setAlarmsOnBoot();*/
    }
}
