
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

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.receivers.AlarmReceiver;

public class AlarmHelper {
    private static AlarmHelper alarmHelper;
    private AlarmManager alarmManager;
    private Realm realm = Realm.getDefaultInstance();

    public synchronized static AlarmHelper getInstance() {
        if (alarmHelper == null) {
            alarmHelper = new AlarmHelper();
            alarmHelper.alarmManager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
        }
        return alarmHelper;
    }

    public void resetAlarms() {
        cancelAllAlarms(App.getInstance().getAlarms());

//        App.getInstance().getAlarms().clear();

        final RealmResults<Alarm> alarmRealmResults = realm.where(Alarm.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                alarmRealmResults.deleteAllFromRealm();
            }
        });

        for (Series series : App.getInstance().getUserList()) {
            makeAlarm(series);
        }
    }

    public void setAlarmsOnBoot() {
        for (Alarm alarm : App.getInstance().getAlarms()) {
            setAlarm(alarm);
        }
    }

    private void setAlarm(Alarm alarm) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getAlarmTime(), createPendingIntent(Integer.valueOf(alarm.getMALID())));
    }

    public void generateNextEpisodeTimes(Series series, int airdate, int simulcastAirdate) {
        DateFormatHelper dateFormatHelper = new DateFormatHelper();

        if (airdate > 0) {
            Calendar airdateCalendar = dateFormatHelper.getCalFromSeconds(airdate);
            calculateNextEpisodeTime(series, airdateCalendar, false);
        }

        if (simulcastAirdate > 0) {
            Calendar simulcastAidateCalendar = dateFormatHelper.getCalFromSeconds(simulcastAirdate);
            calculateNextEpisodeTime(series, simulcastAidateCalendar, true);
        }

    }

    public void calculateNextEpisodeTime(Series series, Calendar calendar, boolean simulcast) {
        Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
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

        if (simulcast) {
            realm.beginTransaction();
            series.setNextEpisodeSimulcastTimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeSimulcastTimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeSimulcastTime(nextEpisode.getTimeInMillis());
            realm.commitTransaction();
        } else {
            realm.beginTransaction();
            series.setNextEpisodeAirtimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeAirtimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeAirtime(nextEpisode.getTimeInMillis());
            realm.commitTransaction();
        }
    }

    public String formatAiringTime(Calendar calendar, boolean prefers24hour) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM d", Locale.getDefault());
        SimpleDateFormat hourFormat;

        String formattedTime = "";

        DateFormatHelper helper = new DateFormatHelper();

        Calendar currentTime = Calendar.getInstance();

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


    public void makeAlarm(final Series series) {
        final long nextEpisodeTime;
        if (series.getNextEpisodeSimulcastTime() != 0L && SharedPrefsHelper.getInstance().prefersSimulcast()) {
            nextEpisodeTime = series.getNextEpisodeSimulcastTime();
        } else {
            nextEpisodeTime = series.getNextEpisodeAirtime();
        }

        Alarm newAlarm = realm.where(Alarm.class).equalTo("MALID", series.getMALID()).findFirst();

        if (newAlarm == null) {
            realm.beginTransaction();

            newAlarm = realm.createObject(Alarm.class, series.getMALID());
            newAlarm.setAlarmTime(nextEpisodeTime);
            newAlarm.setSeries(series);

            realm.commitTransaction();
        }

        setAlarm(newAlarm);
    }

    public void switchAlarmTiming() {
        final RealmResults<Alarm> alarmRealmResults = realm.where(Alarm.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                alarmRealmResults.deleteAllFromRealm();
            }
        });

        for (Series series : App.getInstance().getUserList()) {
            makeAlarm(series);
        }
    }

    private PendingIntent createPendingIntent(int id) {
        Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
        notificationIntent.putExtra("id", id);
        return PendingIntent.getBroadcast(App.getInstance(), id, notificationIntent, 0);
    }

    public void cancelAllAlarms(List<Alarm> alarms) {
        for (Alarm alarm : alarms) {
            alarmManager.cancel(createPendingIntent(Integer.valueOf(alarm.getMALID())));
        }
    }

    public void removeAlarm(final Series series) {
        for (Iterator iterator = App.getInstance().getAlarms().iterator(); iterator.hasNext(); ) {
            Alarm alarm = (Alarm) iterator.next();
            if (alarm.getMALID().equals(series.getMALID())) {
                int id = Integer.valueOf(series.getMALID());
                alarmManager.cancel(createPendingIntent(id));

                realm.beginTransaction();
                alarm.deleteFromRealm();
                realm.commitTransaction();

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
