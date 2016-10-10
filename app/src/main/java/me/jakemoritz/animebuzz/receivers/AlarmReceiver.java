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

        Alarm thisAlarm = null;
        for (Alarm alarm : App.getInstance().getAlarms()){
            if (alarm.getMALID().equals(intentExtra)){
                thisAlarm = alarm;
                break;
            }
        }

        if (thisAlarm != null) {
            Series series = null;

            for (Series eachSeries : App.getInstance().getUserList()) {
                if (eachSeries.getMALID() == thisAlarm.getMALID()) {
                    series = eachSeries;
                    break;
                }
            }

            if (series != null){
                Calendar currentTime = Calendar.getInstance();
                Calendar lastNotificationTime = Calendar.getInstance();
                lastNotificationTime.setTimeInMillis(series.getLastNotificationTime());

                if (currentTime.get(Calendar.DAY_OF_YEAR) != lastNotificationTime.get(Calendar.DAY_OF_YEAR)){
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    NotificationHelper helper = new NotificationHelper();
                    helper.createNewEpisodeNotification(series);

                    App.getInstance().setNotificationReceived(true);

                    App.getInstance().getAlarms().remove(thisAlarm);
                    thisAlarm.deleteFromRealm();
//                    thisAlarm.delete();


                    BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                    backlogItem.setSeries(series);
                    backlogItem.setAlarmTime(thisAlarm.getAlarmTime());
                    backlogItem.setId(Integer.valueOf(series.getMALID()));
//                    backlogItem.save();
                    App.getInstance().getBacklog().add(backlogItem);

                    series.setLastNotificationTime(lastNotificationTime.getTimeInMillis());
                    AlarmHelper.getInstance().makeAlarm(series);

                    if (mainActivity != null){
                        mainActivity.episodeNotificationReceived();
                    }

                    series.setLastNotificationTime(currentTime.getTimeInMillis());

                    realm.commitTransaction();
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


