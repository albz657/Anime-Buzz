package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    private MainActivity mainActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        int intentExtra = intent.getIntExtra("id", -999);

        AlarmHolder thisAlarm = null;
        for (AlarmHolder alarmHolder : App.getInstance().getAlarms()){
            if (alarmHolder.getId() == intentExtra){
                thisAlarm = alarmHolder;
                break;
            }
        }

        if (thisAlarm != null) {
            Series series = null;

            for (Series eachSeries : App.getInstance().getUserAnimeList()) {
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
                    NotificationHelper helper = new NotificationHelper();
                    helper.createNewEpisodeNotification(series);

                    App.getInstance().setNotificationReceived(true);

                    App.getInstance().getAlarms().remove(thisAlarm);
                    thisAlarm.delete();

                    BacklogItem backlogItem = new BacklogItem(series, thisAlarm.getAlarmTime());
                    backlogItem.save();
                    App.getInstance().getBacklog().add(backlogItem);

                    series.setLastNotificationTime(lastNotificationTime.getTimeInMillis());
                    AlarmHelper.getInstance().makeAlarm(series);

                    if (mainActivity != null){
                        mainActivity.episodeNotificationReceived();
                    }

                    series.setLastNotificationTime(currentTime.getTimeInMillis());
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


