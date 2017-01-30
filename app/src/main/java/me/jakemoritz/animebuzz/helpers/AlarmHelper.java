
package me.jakemoritz.animebuzz.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
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

    private void dummyAlarm() {
/*        final RealmResults<Alarm> alarms = App.getInstance().getRealm().where(Alarm.class).findAll();
        if (!alarms.isEmpty()) {
            final long time = System.currentTimeMillis() + 5000L;


            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    alarms.get(0).setAlarmTime(time);

                }
            });
        }*/

        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Series> userList = realm.where(Series.class).findAll();

                for (int i = 0; i < 1; i++){
                    int random = (int) (Math.random() * userList.size() + 1);
                    Series series = userList.get(random);

                    BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                    backlogItem.setSeries(series);
                    backlogItem.setAlarmTime(System.currentTimeMillis());

                    Alarm alarm = realm.createObject(Alarm.class, series.getMALID());
                    alarm.setAlarmTime(System.currentTimeMillis());
                    alarm.setSeries(series);
                }
            }
        });
    }

    public void setAlarmsOnBoot() {
//        dummyAlarm();
        DailyTimeGenerator.getInstance().setNextAlarm(true);
        Realm realm = Realm.getDefaultInstance();
        for (Alarm alarm : realm.where(Alarm.class).findAll()) {
            setAlarm(alarm);
        }
        realm.close();
    }

    private void setAlarm(Alarm alarm) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getAlarmTime(), createPendingIntent(Integer.valueOf(alarm.getMALID())));
    }

    public void generateNextEpisodeTimes(String MALID, int airdate, int simulcastAirdate) {
        DateFormatHelper dateFormatHelper = new DateFormatHelper();

        if (airdate > 0) {
            Calendar airdateCalendar = dateFormatHelper.getCalFromSeconds(airdate);
            calculateNextEpisodeTime(MALID, airdateCalendar, false);
        }

        if (simulcastAirdate > 0) {
            Calendar simulcastAidateCalendar = dateFormatHelper.getCalFromSeconds(simulcastAirdate);
            calculateNextEpisodeTime(MALID, simulcastAidateCalendar, true);
        }
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

    public void calculateNextEpisodeTime(String MALID, Calendar calendar, boolean simulcast) {
        Realm realm = Realm.getDefaultInstance();
        final Series series = realm.where(Series.class).equalTo("MALID", MALID).findFirst();

        final Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
        nextEpisode.set(Calendar.SECOND, 0);
        nextEpisode.set(Calendar.MILLISECOND, 0);

        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.SECOND, 0);
        currentTime.set(Calendar.MILLISECOND, 0);
        if (currentTime.compareTo(nextEpisode) >= 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }

        final String nextEpisodeTimeFormatted = formatAiringTime(nextEpisode, false);
        final String nextEpisodeTimeFormatted24 = formatAiringTime(nextEpisode, true);

        if (simulcast) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    series.setNextEpisodeSimulcastTimeFormatted(nextEpisodeTimeFormatted);
                    series.setNextEpisodeSimulcastTimeFormatted24(nextEpisodeTimeFormatted24);
                    series.setNextEpisodeSimulcastTime(nextEpisode.getTimeInMillis());
                }
            });
        } else {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    series.setNextEpisodeAirtimeFormatted(nextEpisodeTimeFormatted);
                    series.setNextEpisodeAirtimeFormatted24(nextEpisodeTimeFormatted24);
                    series.setNextEpisodeAirtime(nextEpisode.getTimeInMillis());
                }
            });
        }

        realm.close();
    }

    public void calculateNextEpisodeTime(Series series, Calendar calendar, boolean simulcast) {
        final Calendar nextEpisode = Calendar.getInstance();
        nextEpisode.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        nextEpisode.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        nextEpisode.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
        nextEpisode.set(Calendar.SECOND, 0);
        nextEpisode.set(Calendar.MILLISECOND, 0);

        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.SECOND, 0);
        currentTime.set(Calendar.MILLISECOND, 0);
        if (currentTime.compareTo(nextEpisode) >= 0) {
            nextEpisode.add(Calendar.WEEK_OF_MONTH, 1);
        }

        final String nextEpisodeTimeFormatted = formatAiringTime(nextEpisode, false);
        final String nextEpisodeTimeFormatted24 = formatAiringTime(nextEpisode, true);

        if (simulcast) {
            series.setNextEpisodeSimulcastTimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeSimulcastTimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeSimulcastTime(nextEpisode.getTimeInMillis());
        } else {
            series.setNextEpisodeAirtimeFormatted(nextEpisodeTimeFormatted);
            series.setNextEpisodeAirtimeFormatted24(nextEpisodeTimeFormatted24);
            series.setNextEpisodeAirtime(nextEpisode.getTimeInMillis());
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
        if (series.getAiringStatus().equals("Airing")) {
            if (series.getNextEpisodeAirtime() > 0) {
                Calendar airdateCalendar = Calendar.getInstance();
                airdateCalendar.setTimeInMillis(series.getNextEpisodeAirtime());
                calculateNextEpisodeTime(series.getMALID(), airdateCalendar, false);
            }

            if (series.getNextEpisodeSimulcastTime() > 0) {
                Calendar airdateCalendar = Calendar.getInstance();
                airdateCalendar.setTimeInMillis(series.getNextEpisodeSimulcastTime());
                calculateNextEpisodeTime(series.getMALID(), airdateCalendar, true);
            }

            final long nextEpisodeTime;
            if (series.getNextEpisodeSimulcastTime() != 0L && SharedPrefsHelper.getInstance().prefersSimulcast()) {
                nextEpisodeTime = series.getNextEpisodeSimulcastTime();
            } else {
                nextEpisodeTime = series.getNextEpisodeAirtime();
            }

            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Alarm alarm = new Alarm();
                    alarm.setMALID(series.getMALID());
                    alarm.setAlarmTime(nextEpisodeTime);
                    alarm.setSeries(series);

                    realm.insertOrUpdate(alarm);
                }
            });

            Alarm newAlarm = App.getInstance().getRealm().where(Alarm.class).equalTo("MALID", series.getMALID()).findFirst();

            setAlarm(newAlarm);
        } else {
            Log.d("s", "not airing ahh");
        }

    }

    public void switchAlarmTiming() {
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Alarm.class).findAll().deleteAllFromRealm();
            }
        });


        for (Series series : App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll()) {
            makeAlarm(series);
        }
    }

    private PendingIntent createPendingIntent(int id) {
        Intent notificationIntent = new Intent(App.getInstance(), AlarmReceiver.class);
        notificationIntent.putExtra("id", String.valueOf(id));
        return PendingIntent.getBroadcast(App.getInstance(), id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void cancelAllAlarms(List<Alarm> alarms) {
        for (Alarm alarm : alarms) {
            alarmManager.cancel(createPendingIntent(Integer.valueOf(alarm.getMALID())));
        }
    }

    public void removeAlarm(final Series series) {
        Realm realm = Realm.getDefaultInstance();
        final RealmResults<Alarm> alarms = realm.where(Alarm.class).equalTo("MALID", series.getMALID()).findAll();

        int id = Integer.valueOf(series.getMALID());
        alarmManager.cancel(createPendingIntent(id));

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                alarms.deleteAllFromRealm();
            }
        });

        realm.close();
    }

}
