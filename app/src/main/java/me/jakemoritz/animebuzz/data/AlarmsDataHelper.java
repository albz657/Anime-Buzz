package me.jakemoritz.animebuzz.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.AlarmHolder;

public class AlarmsDataHelper {

    private static String TABLE_ALARMS_1 = "TABLE_ALARMS";
    private static String TABLE_ALARMS = "TABLE_ALARMS_2";

    // Alarm columns
    private static final String KEY_ALARM_ID = "alarmid";
    private static final String KEY_ALARM_NAME = "alarmname";
    private static final String KEY_ALARM_TIME = "alarmtime";
    private static final String KEY_ALARM_MALID = "alarmmalid";

    private static AlarmsDataHelper mInstance;

    public String buildAlarmTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_ALARMS +
                "(" + KEY_ALARM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ALARM_NAME + " TEXT," +
                KEY_ALARM_TIME + " INTEGER," +
                KEY_ALARM_MALID + " INTEGER" +
                ")";
    }

    public static synchronized AlarmsDataHelper getInstance() {
        if (mInstance == null) {
            mInstance = new AlarmsDataHelper();
        }
        return mInstance;
    }

    public void upgradeAlarms(SQLiteDatabase database) {
        List<AlarmHolder> oldAlarms = getAllAlarms(database);

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS_1);
        database.execSQL(buildAlarmTable());

        DatabaseHelper.getInstance(App.getInstance()).setUpgrading(false);

        App.getInstance().cancelAllAlarms(oldAlarms);

        for (AlarmHolder alarm : oldAlarms) {
            alarm.setMALID(alarm.getId());
            insertAlarm(alarm, database);
        }

        List<AlarmHolder> upgradedAlarms = getAllAlarms(database);

        App.getInstance().setAlarms(upgradedAlarms);

        App.getInstance().rescheduleAlarms();
    }

    private boolean insertAlarm(AlarmHolder alarm, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ALARM_NAME, alarm.getSeriesName());
        contentValues.put(KEY_ALARM_TIME, alarm.getAlarmTime());
        contentValues.put(KEY_ALARM_MALID, alarm.getMALID());

        database.insert(TABLE_ALARMS, null, contentValues);
        return true;
    }

    private boolean updateAlarm(AlarmHolder alarm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ALARM_NAME, alarm.getSeriesName());
        contentValues.put(KEY_ALARM_TIME, alarm.getAlarmTime());
        contentValues.put(KEY_ALARM_ID, alarm.getId());
        contentValues.put(KEY_ALARM_MALID, alarm.getMALID());

        App.getInstance().getDatabase().update(TABLE_ALARMS, contentValues, KEY_ALARM_ID + " =  ? ", new String[]{String.valueOf(alarm.getId())});
        return true;
    }

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

    private Cursor getAlarmCursor(int alarmId) {
        return App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_ALARMS + " WHERE " + KEY_ALARM_ID + " = ?", new String[]{String.valueOf(alarmId)});
    }

    private AlarmHolder getAlarmWithCursor(Cursor res) {
        int id = res.getInt(res.getColumnIndex(KEY_ALARM_ID));
        long time = res.getLong(res.getColumnIndex(KEY_ALARM_TIME));
        String name = res.getString(res.getColumnIndex(KEY_ALARM_NAME));
        int MALID = -1;

        if (!DatabaseHelper.getInstance(App.getInstance()).isUpgrading()) {
            MALID = res.getInt(res.getColumnIndex(KEY_ALARM_MALID));
        }

        return new AlarmHolder(name, time, id, MALID);
    }

    public void saveAlarm(AlarmHolder alarmHolder) {
        Cursor cursor = getAlarmCursor(alarmHolder.getId());
        if (cursor.getCount() != 0) {
            updateAlarm(alarmHolder);
        } else {
            insertAlarm(alarmHolder, DatabaseHelper.getInstance(App.getInstance()).getWritableDatabase());
        }
        cursor.close();
    }

    public Integer deleteAlarm(int id) {
        return App.getInstance().getDatabase().delete(TABLE_ALARMS, KEY_ALARM_ID + " = ? ", new String[]{String.valueOf(id)});
    }

    public void deleteAllAlarms(SQLiteDatabase database) {
        database.delete(TABLE_ALARMS, null, null);
    }
}
