package me.jakemoritz.animebuzz.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import io.realm.Realm;
import me.jakemoritz.animebuzz.models.Series;

public class DailyTimeGenerator {

    private static DailyTimeGenerator dailyTimeGenerator;
    private AlarmManager alarmManager;

    public synchronized static DailyTimeGenerator getInstance() {
        if (dailyTimeGenerator == null) {
            dailyTimeGenerator = new DailyTimeGenerator();
            dailyTimeGenerator.alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
        }
        return dailyTimeGenerator;
    }

    public void setNextAlarm(){
        long lastUpdateTime = SharedPrefsHelper.getInstance().getLastUpdateTime();

        Calendar currentCalendar = Calendar.getInstance();
        Calendar lastUpdateCalendar = Calendar.getInstance();
        lastUpdateCalendar.setTimeInMillis(lastUpdateTime);

        if ((lastUpdateTime == 0L) || (currentCalendar.get(Calendar.DAY_OF_YEAR) != lastUpdateCalendar.get(Calendar.DAY_OF_YEAR))){
            // Sets calendar to the next day at 12:01:00:00 AM
            Calendar nextAlarmCalendar = Calendar.getInstance();
            nextAlarmCalendar.set(Calendar.HOUR_OF_DAY, 24);
            nextAlarmCalendar.set(Calendar.MINUTE, 1);
            nextAlarmCalendar.set(Calendar.SECOND, 0);
            nextAlarmCalendar.set(Calendar.MILLISECOND, 0);

            Intent timeGeneratorIntent = new Intent(App.getInstance(), DailyReceiver.class);
            timeGeneratorIntent.setAction("GENERATE_TIME");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), 0, timeGeneratorIntent, 0);

            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmCalendar.getTimeInMillis(), pendingIntent);
        }
    }


    public class DailyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("GENERATE_TIME")){
                long time = System.currentTimeMillis();

                Realm realm = Realm.getDefaultInstance();

                for (Series series : realm.where(Series.class).equalTo("airingStatus", "Airing").findAll()) {
                    if (series.getNextEpisodeAirtime() > 0) {
                        Calendar airdateCalendar = Calendar.getInstance();
                        airdateCalendar.setTimeInMillis(series.getNextEpisodeAirtime());
                        AlarmHelper.getInstance().calculateNextEpisodeTime(series.getMALID(), airdateCalendar, false);
                    }

                    if (series.getNextEpisodeSimulcastTime() > 0) {
                        Calendar airdateCalendar = Calendar.getInstance();
                        airdateCalendar.setTimeInMillis(series.getNextEpisodeSimulcastTime());
                        AlarmHelper.getInstance().calculateNextEpisodeTime(series.getMALID(), airdateCalendar, true);
                    }
                }

                realm.close();

                SharedPrefsHelper.getInstance().setLastUpdateTime(time);
            }
        }
    }

}
