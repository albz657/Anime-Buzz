package me.jakemoritz.animebuzz.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import io.realm.Realm;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Series;

// Handles daily update of formatted episode times
public class DailyUpdateUtils {

    private static DailyUpdateUtils dailyUpdateUtils;
    private AlarmManager alarmManager;

    public synchronized static DailyUpdateUtils getInstance() {
        if (dailyUpdateUtils == null) {
            dailyUpdateUtils = new DailyUpdateUtils();
            dailyUpdateUtils.alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
        }
        return dailyUpdateUtils;
    }

    public void setNextAlarm(boolean booted){
        long lastUpdateTime = SharedPrefsUtils.getInstance().getLastUpdateTime();

        Calendar currentCalendar = Calendar.getInstance();
        Calendar lastUpdateCalendar = Calendar.getInstance();
        lastUpdateCalendar.setTimeInMillis(lastUpdateTime);

        if ((lastUpdateTime == 0L) || (currentCalendar.get(Calendar.DAY_OF_YEAR) != lastUpdateCalendar.get(Calendar.DAY_OF_YEAR)) || booted){
            // Sets calendar to the next day at 12:01:00:00 AM
            Calendar nextAlarmCalendar = Calendar.getInstance();
            nextAlarmCalendar.set(Calendar.HOUR_OF_DAY, 24);
            nextAlarmCalendar.set(Calendar.MINUTE, 1);
            nextAlarmCalendar.set(Calendar.SECOND, 0);
            nextAlarmCalendar.set(Calendar.MILLISECOND, 0);

            Intent timeGeneratorIntent = new Intent(App.getInstance(), DailyUpdateReceiver.class);
            timeGeneratorIntent.setAction("GENERATE_TIME");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), 0, timeGeneratorIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmCalendar.getTimeInMillis(), pendingIntent);
        }
    }


    public static class DailyUpdateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("GENERATE_TIME")){
                long time = System.currentTimeMillis();

                Realm realm = Realm.getDefaultInstance();

                for (Series series : realm.where(Series.class).equalTo("airingStatus", "Airing").findAll()) {
                    if (series.getNextEpisodeAirtime() > 0) {
                        Calendar airdateCalendar = Calendar.getInstance();
                        airdateCalendar.setTimeInMillis(series.getNextEpisodeAirtime());
                        AlarmUtils.getInstance().calculateNextEpisodeTime(series.getMALID(), airdateCalendar, false);
                    }

                    if (series.getNextEpisodeSimulcastTime() > 0) {
                        Calendar airdateCalendar = Calendar.getInstance();
                        airdateCalendar.setTimeInMillis(series.getNextEpisodeSimulcastTime());
                        AlarmUtils.getInstance().calculateNextEpisodeTime(series.getMALID(), airdateCalendar, true);
                    }
                }

                realm.close();

                SharedPrefsUtils.getInstance().setLastUpdateTime(time);
            }
        }
    }

}
