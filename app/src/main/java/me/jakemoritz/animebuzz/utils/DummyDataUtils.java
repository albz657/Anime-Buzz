package me.jakemoritz.animebuzz.utils;

import java.util.Calendar;
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
        for (Alarm alarm : alarms){
            AlarmUtils.getInstance().removeAlarm(alarm.getSeries());
        }

        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Alarm.class).findAll().deleteAllFromRealm();


                RealmResults<Series> realmResults = realm.where(Series.class).findAll();

                for (Series series : realmResults){
                    series.setLastNotificationTime(0L);
                }
            }
        });
    }

    public void createNewEpisodeNotification(int notifications){
        RealmResults<Series> seriesList = App.getInstance().getRealm().where(Series.class).findAll();
        Random random = new Random();

        for (int i = 0; i < notifications; i++){
            int randomIndex = random.nextInt(seriesList.size() - 1);
            NotificationUtils.getInstance().createNewEpisodeNotification(seriesList.get(randomIndex));
        }
    }

    public void createChangedTimeNotification(int notifications){
        RealmResults<Series> seriesList = App.getInstance().getRealm().where(Series.class).findAll();
        Random random = new Random();

        for (int i = 0; i < notifications; i++){
            int randomIndex = random.nextInt(seriesList.size() - 1);
            NotificationUtils.getInstance().createChangedTimeNotification(seriesList.get(randomIndex), Calendar.getInstance());
        }
    }

    void createDummyAlarms(final int alarms) {
//        clearAlarms();

        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Series> userList = realm.where(Series.class).findAll();
                realm.where(Alarm.class).findAll().deleteAllFromRealm();
                realm.where(BacklogItem.class).findAll().deleteAllFromRealm();

                final Calendar currentTime = Calendar.getInstance();

                for (int i = 0; i < alarms; i++) {
                    boolean blocked = true;

                    RealmResults<Series> realmResults = App.getInstance().getRealm().where(Series.class).findAll();

                    int curr = 0;
                    Series series;
                    while (blocked) {
                        if (curr >= realmResults.size()) {
                            break;
                        }

                        series = realmResults.get(curr++);

                        final Calendar lastNotificationTime = Calendar.getInstance();
                        lastNotificationTime.setTimeInMillis(series.getLastNotificationTime());

                        if (realm.where(Alarm.class).equalTo("MALID", series.getMALID()).findAll().size() == 0
                                && (series.getLastNotificationTime() == 0 || currentTime.get(Calendar.DAY_OF_YEAR) != lastNotificationTime.get(Calendar.DAY_OF_YEAR))) {
                            BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                            backlogItem.setSeries(series);
                            backlogItem.setAlarmTime(System.currentTimeMillis());

/*                            Alarm alarm = realm.createObject(Alarm.class, series.getMALID());
                            alarm.setAlarmTime(System.currentTimeMillis() + 000L);
                            alarm.setSeries(series);*/
                            blocked = false;
                        }
                    }

                }
            }
        });
    }

}
