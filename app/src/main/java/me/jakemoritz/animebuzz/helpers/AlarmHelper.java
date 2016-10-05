package me.jakemoritz.animebuzz.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class AlarmHelper {
    private static AlarmHelper alarmHelper;
    private AlarmManager alarmManager;

    public synchronized static AlarmHelper getInstance() {
        if (alarmHelper == null) {
            alarmHelper = new AlarmHelper();
            alarmHelper.alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
        }
        return alarmHelper;
    }

    public void resetAlarms() {
        cancelAllAlarms(App.getInstance().getAlarms());

        App.getInstance().getAlarms().clear();

        AlarmHolder.deleteAll(AlarmHolder.class);

        for (Series series : App.getInstance().getUserAnimeList()) {
            makeAlarm(series);
        }
    }

    public void setAlarmsOnBoot() {
        for (AlarmHolder alarm : App.getInstance().getAlarms()) {
            setAlarm(alarm);
        }
    }

    private void setAlarm(AlarmHolder alarm) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getAlarmTime(), createPendingIntent(alarm.getId().intValue()));
    }

    public Calendar generateNextEpisodeTimes(Series series, boolean prefersSimulcast) {
        if ((prefersSimulcast && series.getSimulcast_airdate() < 0) || (series.getAirdate() < 0) || (!series.getShowType().equals("TV") && !series.getShowType().isEmpty())) {
            return null;
        }

        DateFormatHelper dateFormatHelper = new DateFormatHelper();

        Calendar initialAirTime;
        if (prefersSimulcast) {
            initialAirTime = dateFormatHelper.getCalFromSeconds(series.getSimulcast_airdate());
        } else {
            initialAirTime = dateFormatHelper.getCalFromSeconds(series.getAirdate());
        }

        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, initialAirTime.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, initialAirTime.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, initialAirTime.get(Calendar.DAY_OF_WEEK));
        nextEpisode.set(Calendar.SECOND, 0);
        nextEpisode.set(Calendar.MILLISECOND, 0);

        if (!App.getInstance().isNotificationReceived()) {
            Calendar currentTime = Calendar.getInstance();
            currentTime.set(Calendar.SECOND, 0);
            currentTime.set(Calendar.MILLISECOND, 0);
            if (currentTime.compareTo(nextEpisode) >= 0) {
                nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
            }
        } else {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
            App.getInstance().setNotificationReceived(false);
        }

        String nextEpisodeTimeFormatted = formatAiringTime(nextEpisode, false);
        String nextEpisodeTimeFormatted24 = formatAiringTime(nextEpisode, true);

        if (prefersSimulcast) {
            series.setNextEpisodeSimulcastTimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeSimulcastTimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeSimulcastTime(nextEpisode.getTimeInMillis());
        } else {
            series.setNextEpisodeAirtimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeAirtimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeAirtime(nextEpisode.getTimeInMillis());
        }

        return nextEpisode;
    }

    public String formatAiringTime(Calendar calendar, boolean prefers24hour) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM d", Locale.getDefault());
        SimpleDateFormat hourFormat;

        String formattedTime = "";

        DateFormatHelper helper = new DateFormatHelper();

        Calendar currentTime = Calendar.getInstance();

        //DEBUG
//        calendar.setTimeInMillis(1473047450000L);

        if (currentTime.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            int dayDiff = calendar.get(Calendar.DAY_OF_YEAR) - currentTime.get(Calendar.DAY_OF_YEAR);

            if (dayDiff <= 1) {
                // yesterday, today, tomorrow OR x days ago
                formattedTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
            } else if (dayDiff >= 2 && dayDiff <= 6) {
                // day of week
                formattedTime = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            } else if (dayDiff == 7) {
                formattedTime = "Next " + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            } else {
                // normal date
                formattedTime = format.format(calendar.getTime());
                formattedTime += helper.getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH));
            }
        }


        /*formattedTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(), 0, DateUtils.FORMAT_SHOW_WEEKDAY).toString();
        formattedTime = DateUtils.getRelativeTimeSpanString(System.currentTimeMillis(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_WEEKDAY).toString();
        formattedTime = DateUtils.getRelativeTimeSpanString(1473259168106L, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_WEEKDAY).toString();
        formattedTime = DateUtils.getRelativeTimeSpanString(1473220250000L, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_WEEKDAY).toString();*/

        if (prefers24hour) {
            hourFormat = new SimpleDateFormat(", kk:mm", Locale.getDefault());
            formattedTime += hourFormat.format(calendar.getTime());

        } else {
            hourFormat = new SimpleDateFormat(", h:mm", Locale.getDefault());
            formattedTime += hourFormat.format(calendar.getTime());
            formattedTime += new SimpleDateFormat(" a", Locale.getDefault()).format(calendar.getTime());
        }

        return formattedTime;
    }


    public void makeAlarm(Series series) {
        Calendar nextEpisode = generateNextEpisodeTimes(series, SharedPrefsHelper.getInstance().prefersSimulcast());

        AlarmHolder newAlarm = new AlarmHolder(series.getName(), nextEpisode.getTimeInMillis(), series.getMALID().intValue());
        newAlarm.save();

        setAlarm(newAlarm);

        App.getInstance().getAlarms().add(newAlarm);

        series.save();
    }

    public void switchAlarmTiming() {
        App.getInstance().getAlarms().clear();

        AlarmHolder.deleteAll(AlarmHolder.class);

        for (Series series : App.getInstance().getUserAnimeList()) {
            makeAlarm(series);
        }
    }

    private PendingIntent createPendingIntent(int id) {
        Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
        notificationIntent.putExtra("id", id);
        return PendingIntent.getBroadcast(App.getInstance(), id, notificationIntent, 0);
    }

    public void cancelAllAlarms(List<AlarmHolder> alarms) {
        for (AlarmHolder alarmHolder : App.getInstance().getAlarms()) {
            alarmManager.cancel(createPendingIntent(alarmHolder.getId().intValue()));
        }
    }

    public void removeAlarm(Series series) {
        int id;
        AlarmHolder alarmHolder;
        for (Iterator iterator = App.getInstance().getAlarms().iterator(); iterator.hasNext(); ) {
            alarmHolder = (AlarmHolder) iterator.next();
            if (alarmHolder.getMALID() == series.getMALID()) {
                id = alarmHolder.getId().intValue();
                alarmManager.cancel(createPendingIntent(id));
                alarmHolder.delete();
                iterator.remove();
            }
        }
    }

    private void dummyAlarm() {
        if (!App.getInstance().getAlarms().isEmpty()) {
            long time = System.currentTimeMillis();
            time += 5000L;
            App.getInstance().getAlarms().get(0).setAlarmTime(time);
        }
    }

}
