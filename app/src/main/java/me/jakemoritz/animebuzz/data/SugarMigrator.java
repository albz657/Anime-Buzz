package me.jakemoritz.animebuzz.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;


public class SugarMigrator {

    // Table names
    private static final String TABLE_SERIES = "SERIES";
    private static final String TABLE_SEASON = "SEASON_METADATA";
    private static final String TABLE_BACKLOG = "BACKLOG_ITEM";
    private static final String TABLE_ALARM = "ALARM_HOLDER";

    // Alarm columns
    private static final String ALARM_ID = "ID";
    private static final String ALARM_MALID = "MALID";
    private static final String ALARM_TIME = "ALARM_TIME";
    private static final String SERIES_NAME = "SERIES_NAME";


    // Backlog columns
    private static final String BACKLOG_ID = "ID";
    private static final String BACKLOG_TIME = "ALARM_TIME";
    private static final String SERIES = "SERIES";

    // Season columns
    private static final String KEY = "KEY";
    private static final String SEASON_NAME = "NAME";
    private static final String START_TIMESTAMP = "STARTTIMESTAMP";

    // Anime columns
    private static final String ID = "ID";
    private static final String ANNID = "ANNID";
    private static final String AIRDATE = "AIRDATE";
    private static final String AIRING_STATUS = "AIRING_STATUS";
    private static final String ENGLISH_TITLE = "ENGLISH_TITLE";
    private static final String EPISODES_WATCHED = "EPISODES_WATCHED";
    private static final String FINISHED_AIRING_DATE = "FINISHED_AIRING_DATE";
    private static final String IS_IN_USER_LIST = "IS_IN_USER_LIST";
    private static final String LAST_NOTIFICATION_TIME = "LAST_NOTIFICATION_TIME";
    private static final String NAME = "NAME";
    private static final String NEXT_EPISODE_AIRTIME = "NEXT_EPISODE_AIRTIME";
    private static final String NEXT_EPISODE_AIRTIME_FORMATTED = "NEXT_EPISODE_AIRTIME_FORMATTED";
    private static final String NEXT_EPISODE_AIRTIME_FORMATTED24 = "NEXT_EPISODE_AIRTIME_FORMATTED24";
    private static final String NEXT_EPISODE_SIMULCAST_TIME = "NEXT_EPISODE_SIMULCAST_TIME";
    private static final String NEXT_EPISODE_SIMULCAST_TIME_FORMATTED = "NEXT_EPISODE_SIMULCAST_TIME_FORMATTED";
    private static final String NEXT_EPISODE_SIMULCAST_TIME_FORMATTED24 = "NEXT_EPISODE_SIMULCAST_TIME_FORMATTED24";
    private static final String SEASON = "SEASON";
    private static final String SHOW_TYPE = "SHOW_TYPE";
    private static final String SIMULCAST = "SIMULCAST";
    private static final String SIMULCASTAIRDATE = "SIMULCASTAIRDATE";
    private static final String SIMULCASTDELAY = "SIMULCASTDELAY";
    private static final String SINGLE = "SINGLE";
    private static final String STARTED_AIRING_DATE = "STARTED_AIRING_DATE";

    public static void migrateToRealm() {
        SQLiteDatabase sugarDb = SQLiteDatabase.openDatabase(App.getInstance().getDatabasePath("buzz_sugar.db").getPath(), null, 0);
        migrateSeason(sugarDb);
        migrateSeries(sugarDb);
        migrateBacklog(sugarDb);
        migrateAlarms(sugarDb);
    }

    private static void migrateSeason(SQLiteDatabase sugarDb) {
        Cursor cursor = sugarDb.rawQuery("SELECT * FROM " + TABLE_SEASON, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            final String seasonKey = cursor.getString(cursor.getColumnIndex(KEY));
            final String seasonName = cursor.getString(cursor.getColumnIndex(SEASON_NAME));
            final String startTimeStamp = cursor.getString(cursor.getColumnIndex(START_TIMESTAMP));

            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Season season = App.getInstance().getRealm().createObject(Season.class, seasonKey);
                    season.setName(seasonName);
                    season.setStartDate(startTimeStamp);
                }
            });

            cursor.moveToNext();
        }

        RealmResults<Season> seasonRealmResults = App.getInstance().getRealm().where(Season.class).findAll();

        for (final Season season : seasonRealmResults) {
            final String relativeTime = Season.calculateRelativeTime(season.getName());
            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    season.setRelativeTime(relativeTime);
                }
            });
        }
        cursor.close();
    }

    private static void migrateSeries(SQLiteDatabase sugarDb) {
        Cursor cursor = sugarDb.rawQuery("SELECT * FROM " + TABLE_SERIES, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            int id = cursor.getInt(cursor.getColumnIndex(ID));
            final String MALID = String.valueOf(id);
            int annid = cursor.getInt(cursor.getColumnIndex(ANNID));
            final String ANNID = String.valueOf(annid);
            int airdate = cursor.getInt(cursor.getColumnIndex(AIRDATE));
            final int episodesWatched = cursor.getInt(cursor.getColumnIndex(EPISODES_WATCHED));
            final int isInUserList = cursor.getInt(cursor.getColumnIndex(IS_IN_USER_LIST));
            final int lastNotificationTime = cursor.getInt(cursor.getColumnIndex(LAST_NOTIFICATION_TIME));
            final int nextEpisodeAirtime = cursor.getInt(cursor.getColumnIndex(NEXT_EPISODE_AIRTIME));
            final int nextEpisodeSimulcastTime = cursor.getInt(cursor.getColumnIndex(NEXT_EPISODE_SIMULCAST_TIME));
            int simulcastAirdate = cursor.getInt(cursor.getColumnIndex(SIMULCASTAIRDATE));
            final int single = cursor.getInt(cursor.getColumnIndex(SINGLE));

            final String name = cursor.getString(cursor.getColumnIndex(NAME));
            final String airingStatus = cursor.getString(cursor.getColumnIndex(AIRING_STATUS));
            String englishTitle = cursor.getString(cursor.getColumnIndex(ENGLISH_TITLE));
            if (englishTitle.isEmpty()) {
                englishTitle = name;
            }
            final String finalEnglishTitle = englishTitle;
            final String finishedAiringDate = cursor.getString(cursor.getColumnIndex(FINISHED_AIRING_DATE));
            final String nextEpisodeAirtimeFormatted = cursor.getString(cursor.getColumnIndex(NEXT_EPISODE_AIRTIME_FORMATTED));
            final String nextEpisodeAirtimeFormatted24 = cursor.getString(cursor.getColumnIndex(NEXT_EPISODE_AIRTIME_FORMATTED24));
            final String nextEpisodeSimulcastTimeFormatted = cursor.getString(cursor.getColumnIndex(NEXT_EPISODE_SIMULCAST_TIME_FORMATTED));
            final String nextEpisodeSimulcastTimeFormatted24 = cursor.getString(cursor.getColumnIndex(NEXT_EPISODE_SIMULCAST_TIME_FORMATTED24));
            String seasonName = cursor.getString(cursor.getColumnIndex(SEASON));
            final String showType = cursor.getString(cursor.getColumnIndex(SHOW_TYPE));
            final String startedAiringDate = cursor.getString(cursor.getColumnIndex(STARTED_AIRING_DATE));
            final String simulcast = cursor.getString(cursor.getColumnIndex(SIMULCAST));

            final double simulcastDelay = cursor.getDouble(cursor.getColumnIndex(SIMULCASTDELAY));

            final Season season = App.getInstance().getRealm().where(Season.class).equalTo("name", seasonName).findFirst();
            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Series series = App.getInstance().getRealm().createObject(Series.class, MALID);
                    series.setANNID(ANNID);
                    series.setName(name);
                    series.setAiringStatus(airingStatus);
                    series.setFinishedAiringDate(finishedAiringDate);
                    series.setNextEpisodeAirtime(nextEpisodeAirtime);
                    series.setNextEpisodeAirtimeFormatted(nextEpisodeAirtimeFormatted);
                    series.setNextEpisodeAirtimeFormatted24(nextEpisodeAirtimeFormatted24);
                    series.setNextEpisodeSimulcastTimeFormatted(nextEpisodeSimulcastTimeFormatted);
                    series.setNextEpisodeSimulcastTimeFormatted24(nextEpisodeSimulcastTimeFormatted24);
                    series.setNextEpisodeSimulcastTime(nextEpisodeSimulcastTime);
                    series.setSimulcastProvider(simulcast);
                    series.setSimulcastDelay(simulcastDelay);
                    series.setStartedAiringDate(startedAiringDate);
                    series.setShowType(showType);
                    series.setInUserList(isInUserList == 1);
                    series.setLastNotificationTime(lastNotificationTime);
                    series.setSingle(single == 1);
                    series.setEpisodesWatched(episodesWatched);
                    series.setSeason(season);
                    series.setEnglishTitle(finalEnglishTitle);
                    season.getSeasonSeries().add(series);
                }
            });

            Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", MALID).findFirst();

            if (airingStatus.equals("Airing")) {
                AlarmHelper.getInstance().generateNextEpisodeTimes(series, airdate, simulcastAirdate);
            }

            cursor.moveToNext();
        }
        cursor.close();
    }

    private static void migrateBacklog(SQLiteDatabase sugarDb) {
        Cursor cursor = sugarDb.rawQuery("SELECT * FROM " + TABLE_BACKLOG, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            final long alarmTime = cursor.getLong(cursor.getColumnIndex(BACKLOG_TIME));
            int seriesMALID = cursor.getInt(cursor.getColumnIndex(SERIES));
            final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", String.valueOf(seriesMALID)).findFirst();

            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    BacklogItem backlogItem = App.getInstance().getRealm().createObject(BacklogItem.class);
                    backlogItem.setAlarmTime(alarmTime);
                    backlogItem.setSeries(series);
                }
            });

            cursor.moveToNext();
        }

        cursor.close();
    }

    private static void migrateAlarms(SQLiteDatabase sugarDb) {
        Cursor cursor = sugarDb.rawQuery("SELECT * FROM " + TABLE_ALARM, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            final long alarmTime = cursor.getLong(cursor.getColumnIndex(ALARM_TIME));
            final int seriesMALID = cursor.getInt(cursor.getColumnIndex(ALARM_MALID));
            final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", String.valueOf(seriesMALID)).findFirst();

            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        Alarm alarm = App.getInstance().getRealm().createObject(Alarm.class, String.valueOf(seriesMALID));
                        alarm.setAlarmTime(alarmTime);
                        alarm.setSeries(series);
                    } catch (RealmPrimaryKeyConstraintException e) {
                        e.printStackTrace();
                    }
                }
            });

            cursor.moveToNext();
        }

        cursor.close();
    }
}
