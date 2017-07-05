package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import io.realm.Realm;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.utils.AlarmUtils;
import me.jakemoritz.animebuzz.utils.DailyTimeGenerator;
import me.jakemoritz.animebuzz.utils.NotificationUtils;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

public class EpisodeNotificationReceiver extends BroadcastReceiver {

    private final static String TAG = EpisodeNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentExtra = intent.getStringExtra("id");

        Realm realm = Realm.getDefaultInstance();

        final Alarm thisAlarm = realm.where(Alarm.class).equalTo("MALID", intentExtra).findFirst();
        final Series series = realm.where(Series.class).equalTo("MALID", intentExtra).findFirst();

        if (thisAlarm != null && series != null) {
            final Calendar currentTime = Calendar.getInstance();
            final Calendar lastNotificationTime = Calendar.getInstance();
            lastNotificationTime.setTimeInMillis(series.getLastNotificationTime());

            // Check if notification for this episode was already displayed today (protects against a bug where PendingIntent is sent twice)
            if (series.getLastNotificationTime() == 0 || currentTime.get(Calendar.DAY_OF_YEAR) != lastNotificationTime.get(Calendar.DAY_OF_YEAR)) {
                boolean prefersNotification = SharedPrefsUtils.getInstance().episodeNotificationsEnabled();

                // Create new BacklogItem for episode release
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

                // Display episode notification if enabled
                if (prefersNotification) {
                    NotificationUtils helper = new NotificationUtils();
                    helper.createNewEpisodeNotification(series);
                }

                // Notify MainActivity that episode notification received, must update Backlog count badges
                context.sendBroadcast(new Intent("NOTIFICATION_RECEIVED"));

                // Create new Alarm for Series
                AlarmUtils.getInstance().makeAlarm(series);
            }
        }

        realm.close();

        // Device just booted, set all Alarms again
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            AlarmUtils.getInstance().setAlarmsOnBoot();
            DailyTimeGenerator.getInstance().setNextAlarm(false);
        }
    }
}


