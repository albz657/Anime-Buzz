package me.jakemoritz.animebuzz.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.realm.Realm;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.Series;

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

    void upgradeAlarms(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ALARMS, null);

        Realm realm = Realm.getDefaultInstance();

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            final Long id = cursor.getLong(cursor.getColumnIndex(KEY_ALARM_ID));
            final long time = cursor.getLong(cursor.getColumnIndex(KEY_ALARM_TIME));
            final String name = cursor.getString(cursor.getColumnIndex(KEY_ALARM_NAME));
            cursor.moveToNext();

            final Series series = realm.where(Series.class).equalTo("MALID", String.valueOf(id)).findFirst();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Alarm alarm = realm.createObject(Alarm.class, String.valueOf(id));
                    alarm.setSeries(series);
                    alarm.setAlarmTime(time);
                }
            });
        }

        realm.close();
        cursor.close();
    }
}
