package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import io.realm.Realm;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    private MainActivity mainActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentExtra = intent.getStringExtra("id");

        Realm realm = Realm.getDefaultInstance();

        Alarm thisAlarm = realm.where(Alarm.class).equalTo("MALID", intentExtra).findFirst();

        if (thisAlarm != null) {
            Series series = realm.where(Series.class).equalTo("MALID", intentExtra).findFirst();

            if (series != null){
                Calendar currentTime = Calendar.getInstance();
                Calendar lastNotificationTime = Calendar.getInstance();
                lastNotificationTime.setTimeInMillis(series.getLastNotificationTime());

                if (currentTime.get(Calendar.DAY_OF_YEAR) != lastNotificationTime.get(Calendar.DAY_OF_YEAR)){

                    NotificationHelper helper = new NotificationHelper();
                    helper.createNewEpisodeNotification(series);

                    App.getInstance().setNotificationReceived(true);

                    realm.beginTransaction();

                    thisAlarm.deleteFromRealm();

                    BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                    backlogItem.setSeries(series);
                    backlogItem.setAlarmTime(thisAlarm.getAlarmTime());

                    series.setLastNotificationTime(lastNotificationTime.getTimeInMillis());

                    realm.commitTransaction();

                    AlarmHelper.getInstance().makeAlarm(series);

                    if (mainActivity != null){
                        mainActivity.episodeNotificationReceived();
                    }
                }
            }
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            AlarmHelper.getInstance().setAlarmsOnBoot();
        }
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}


