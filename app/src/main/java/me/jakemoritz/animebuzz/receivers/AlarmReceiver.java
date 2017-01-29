package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import io.realm.Realm;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.DailyTimeGenerator;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.helpers.WakeLocker;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    private final static String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLocker.acquire(context);

        String intentExtra = intent.getStringExtra("id");

        Realm realm = Realm.getDefaultInstance();

        final Alarm thisAlarm = realm.where(Alarm.class).equalTo("MALID", intentExtra).findFirst();

        if (thisAlarm != null) {
            final Series series = realm.where(Series.class).equalTo("MALID", intentExtra).findFirst();

            if (series != null){
                final Calendar currentTime = Calendar.getInstance();
                final Calendar lastNotificationTime = Calendar.getInstance();
                lastNotificationTime.setTimeInMillis(series.getLastNotificationTime());

                if (series.getLastNotificationTime() == 0 || currentTime.get(Calendar.DAY_OF_YEAR) != lastNotificationTime.get(Calendar.DAY_OF_YEAR)){
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean prefersNotification = sharedPreferences.getBoolean(context.getString(R.string.pref_notification_key), true);

                    if (prefersNotification) {
                        NotificationHelper helper = new NotificationHelper();
                        helper.createNewEpisodeNotification(series);
                    }

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            long alarmTime = thisAlarm.getAlarmTime();

                            BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                            backlogItem.setSeries(series);
                            backlogItem.setAlarmTime(alarmTime);

                            series.setLastNotificationTime(currentTime.getTimeInMillis());
                        }
                    });

                    context.sendBroadcast(new Intent("NOTIFICATION_RECEIVED"));
                    context.sendBroadcast(new Intent("ACTION_APPWIDGET_UPDATE"));

                    AlarmHelper.getInstance().makeAlarm(series);
                }
            }
        }

        realm.close();

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            AlarmHelper.getInstance().setAlarmsOnBoot();
            DailyTimeGenerator.getInstance().setNextAlarm(false);
        }

        WakeLocker.release();
    }
}


