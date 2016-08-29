package me.jakemoritz.animebuzz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.NotificationHelper;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int intentExtra = intent.getIntExtra("MALID", -1);
        if (intentExtra > 0) {
            Series series = null;

            for (Series eachSeries : App.getInstance().getUserAnimeList()) {
                if (eachSeries.getMALID() == intentExtra) {
                    series = eachSeries;
                    break;
                }
            }

            if (series != null){
                NotificationHelper helper = new NotificationHelper();
                helper.createNewEpisodeNotification(series);

                long time = System.currentTimeMillis();

                for (AlarmHolder alarmHolder : App.getInstance().getAlarms().values()){
                    if (alarmHolder.getId() == intentExtra){
                        time = alarmHolder.getAlarmTime();
                        break;
                    }
                }

                App.getInstance().setNotificationReceived(true);

                series.getBacklog().add(time);
                App.getInstance().getBacklog().add(new BacklogItem(series, time));
                App.getInstance().makeAlarm(series);


                if (App.getInstance().getMainActivity() != null){
                    App.getInstance().getMainActivity().episodeNotificationReceived();
                }
            }
        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            App.getInstance().rescheduleAlarms();
        }
    }

}


