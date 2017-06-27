package me.jakemoritz.animebuzz.data.migrations;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.realm.Realm;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import me.jakemoritz.animebuzz.misc.App;
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

    public void migrateAlarms(SQLiteDatabase sugarDb) {
        Cursor cursor = sugarDb.rawQuery("SELECT * FROM " + TABLE_ALARMS, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            final long alarmTime = cursor.getLong(cursor.getColumnIndex(KEY_ALARM_TIME));
            final int seriesMALID = cursor.getInt(cursor.getColumnIndex(KEY_ALARM_ID));
            final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", String.valueOf(seriesMALID)).findFirst();

            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        Alarm alarm = App.getInstance().getRealm().createObject(Alarm.class, String.valueOf(seriesMALID));
                        alarm.setAlarmTime(alarmTime);
                        alarm.setSeries(series);
                    } catch (RealmPrimaryKeyConstraintException e) {
                        e.printStackTrace();
                    }
                }
            });

            cursor.moveToNext();
        }

        cursor.close();
    }
}
