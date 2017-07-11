package me.jakemoritz.animebuzz.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class DummyDataUtils {
    private static DummyDataUtils dummyDataUtils;

    public synchronized static DummyDataUtils getInstance() {
        if (dummyDataUtils == null) {
            dummyDataUtils = new DummyDataUtils();
        }
        return dummyDataUtils;
    }

    private void clearAlarms() {
        RealmResults<Alarm> alarms = App.getInstance().getRealm().where(Alarm.class).findAll();
        for (Alarm alarm : alarms) {
            AlarmUtils.getInstance().removeAlarm(alarm.getSeries());
        }

        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Alarm.class).findAll().deleteAllFromRealm();


                RealmResults<Series> realmResults = realm.where(Series.class).findAll();

                for (Series series : realmResults) {
                    series.setLastNotificationTime(0L);
                }
            }
        });
    }

    private void clearBacklogItems() {
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(BacklogItem.class).findAll().deleteAllFromRealm();
            }
        });
    }

    public void createNewEpisodeNotification(int notifications) {
        RealmResults<Series> seriesList = App.getInstance().getRealm().where(Series.class).findAll();
        Random random = new Random();

        for (int i = 0; i < notifications; i++) {
            int randomIndex = random.nextInt(seriesList.size() - 1);
            NotificationUtils.getInstance().createNewEpisodeNotification(seriesList.get(randomIndex));
        }
    }

    public void createChangedTimeNotification(int notifications) {
        RealmResults<Series> seriesList = App.getInstance().getRealm().where(Series.class).findAll();
        Random random = new Random();

        for (int i = 0; i < notifications; i++) {
            int randomIndex = random.nextInt(seriesList.size() - 1);
            NotificationUtils.getInstance().createChangedTimeNotification(seriesList.get(randomIndex), Calendar.getInstance());
        }
    }

    public void createLongNameNewEpisodeNotification() {
        RealmResults<Series> seriesList = App.getInstance().getRealm().where(Series.class).findAll();

        for (Series series : seriesList) {
            if (series.getName().length() > 40) {
                NotificationUtils.getInstance().createChangedTimeNotification(series, Calendar.getInstance());
                break;
            }
        }
    }

    public void createDummyAlarms(final int alarms) {
        clearAlarms();

        final long[] timeArray = new long[]{1499612400000L, 1499450400000L, 1499693400000L, 1499206500000L, 1498926600000L};

        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Alarm.class).findAll().deleteAllFromRealm();
                realm.where(BacklogItem.class).findAll().deleteAllFromRealm();

                final Calendar currentTime = Calendar.getInstance();

                RealmResults<Series> realmResults = App.getInstance().getRealm().where(Series.class).equalTo("seasonKey", SharedPrefsUtils.getInstance().getLatestSeasonKey()).findAll();
                Map<String, Integer> simulcastMap = new HashMap<String, Integer>();
                Random random = new Random();
                int curr = 0;
                for (int i = 0; i < alarms; i++) {
                    while (true) {
                        if (curr >= realmResults.size()) {
                            break;
                        }

                        Series series = realmResults.get(random.nextInt(realmResults.size() - 1));

                        final Calendar lastNotificationTime = Calendar.getInstance();
                        lastNotificationTime.setTimeInMillis(series.getLastNotificationTime());

                        String seriesSimulcast = series.getSimulcastProvider();
                        int simulcastCount = 0;
                        if (simulcastMap.containsKey(seriesSimulcast)){
                            simulcastCount = simulcastMap.get(seriesSimulcast);
                            simulcastMap.put(seriesSimulcast, simulcastCount + 1);
                            simulcastCount = simulcastMap.get(seriesSimulcast);
                        } else {
                            simulcastMap.put(seriesSimulcast, 1);
                        }

                        RealmResults<Alarm> alarmsForSeries = realm.where(Alarm.class).equalTo("MALID", series.getMALID()).findAll();
                        if (simulcastCount < 2 && !series.getSimulcastProvider().equals("false") && alarmsForSeries.size() == 0 && (series.getLastNotificationTime() == 0 || currentTime.get(Calendar.DAY_OF_YEAR) != lastNotificationTime.get(Calendar.DAY_OF_YEAR))
                                && !(series.getAiringStatus().equals(Series.AIRING_STATUS_FINISHED_AIRING) || !series.getShowType().equals("TV") && (!series.isSingle() || (series.isSingle() && (series.getStartedAiringDate().isEmpty() && series.getFinishedAiringDate().isEmpty()))))) {
                            long time = timeArray[curr++];
                            BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                            backlogItem.setSeries(series);
                            backlogItem.setAlarmTime(time);

/*                            Alarm alarm = realm.createObject(Alarm.class, series.getMALID());
                            alarm.setAlarmTime(System.currentTimeMillis() + 10000L);
                            alarm.setSeries(series);*/
                            break;
                        }
                    }

                }
            }
        });
    }

}
