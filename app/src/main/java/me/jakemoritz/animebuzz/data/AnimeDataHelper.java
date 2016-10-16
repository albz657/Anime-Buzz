package me.jakemoritz.animebuzz.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;

public class AnimeDataHelper {

    private static final String TAG = AnimeDataHelper.class.getSimpleName();

    private static String TABLE_ANIME = "TABLE_ANIME";

    // Anime columns
    private static final String KEY_AIRDATE = "airdate";
    private static final String KEY_NAME = "title";
    private static final String KEY_MALID = "_id";
    private static final String KEY_SIMULCAST_AIRDATE = "simulcastairdate";
    private static final String KEY_IS_IN_USER_LIST = "isinuserlist";
    private static final String KEY_ANIME_SEASON = "animeseason";
    private static final String KEY_CURRENTLY_AIRING = "iscurrentlyairing";
    private static final String KEY_ANNID = "ANNID";
    private static final String KEY_SIMULCAST_DELAY = "simulcastdelay";
    private static final String KEY_SIMULCAST = "simulcast";
    private static final String KEY_BACKLOG = "backlog";
    private static final String KEY_NEXT_EPISODE_AIRTIME = "nextepisodeairtime";
    private static final String KEY_NEXT_EPISODE_SIMULCAST_AIRTIME = "nextepisodesimulcastairtime";
    private static final String KEY_EPISODES_WATCHED = "episodeswatched";
    private static final String KEY_NEXT_EPISODE_AIRTIME_FORMATTED = "nextepisodeairtimeformatted";
    private static final String KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED = "nextepisodesimulcastairtimeformatted";
    private static final String KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24 = "nextepisodeairtimeformatted_twofour";
    private static final String KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24 = "nextepisodesimulcastairtimeformatted_twofour";

    private static AnimeDataHelper mInstance;

    private List<BacklogItem> oldBacklogItems;

    public static synchronized AnimeDataHelper getInstance() {
        if (mInstance == null) {
            mInstance = new AnimeDataHelper();
        }
        return mInstance;
    }

    public void migrateSeries(SQLiteDatabase sugarDb) {
        Realm realm = Realm.getDefaultInstance();
        Cursor cursor = sugarDb.rawQuery("SELECT * FROM " + TABLE_ANIME, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            int id = cursor.getInt(cursor.getColumnIndex(KEY_MALID));
            final String MALID = String.valueOf(id);
            int annid = cursor.getInt(cursor.getColumnIndex(KEY_ANNID));
            final String ANNID = String.valueOf(annid);
            int airdate = cursor.getInt(cursor.getColumnIndex(KEY_AIRDATE));
            final int episodesWatched = cursor.getInt(cursor.getColumnIndex(KEY_EPISODES_WATCHED));
            final int isInUserList = cursor.getInt(cursor.getColumnIndex(KEY_IS_IN_USER_LIST));
            final int lastNotificationTime = 0;
            final int nextEpisodeAirtime = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME));
            final int nextEpisodeSimulcastTime = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME));
            int simulcastAirdate = cursor.getInt(cursor.getColumnIndex(KEY_SIMULCAST_AIRDATE));
            final int single = 0;

            final String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            int isCurrentlyAiring = cursor.getInt(cursor.getColumnIndex("iscurrentlyairing"));

            String airingStatus = "";
            if (isCurrentlyAiring == 1) {
                airingStatus = "Airing";
            }

            final String finishedAiringDate = "";
            final String nextEpisodeAirtimeFormatted = cursor.getString(cursor.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME_FORMATTED));
            final String nextEpisodeAirtimeFormatted24 = cursor.getString(cursor.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24));
            final String nextEpisodeSimulcastTimeFormatted = cursor.getString(cursor.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED));
            final String nextEpisodeSimulcastTimeFormatted24 = cursor.getString(cursor.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24));
            String seasonName = cursor.getString(cursor.getColumnIndex(KEY_ANIME_SEASON));
            final String showType = "";
            final String startedAiringDate = "";
            final String simulcast = cursor.getString(cursor.getColumnIndex(KEY_SIMULCAST));

            final double simulcastDelay = cursor.getDouble(cursor.getColumnIndex(KEY_SIMULCAST_DELAY));

            final Season season = realm.where(Season.class).equalTo("name", seasonName).findFirst();

            Type type = new TypeToken<ArrayList<Long>>() {
            }.getType();
            List<Long> backlog = new Gson().fromJson(cursor.getString(cursor.getColumnIndex(KEY_BACKLOG)), type);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Series series = realm.createObject(Series.class, MALID);
                    series.setANNID(ANNID);
                    series.setName(name);
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
                    series.setEnglishTitle(name);
                    season.getSeasonSeries().add(series);
                }
            });

            final Series series = realm.where(Series.class).equalTo("MALID", MALID).findFirst();

            for (final Long alarmTime : backlog){
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        BacklogItem backlogItem = realm.createObject(BacklogItem.class);
                        backlogItem.setAlarmTime(alarmTime);
                        backlogItem.setSeries(series);
                    }
                });
            }

            realm.beginTransaction();
            series.setAiringStatus(airingStatus);
            realm.commitTransaction();

            if (airingStatus.equals("Airing")) {
                AlarmHelper.getInstance().generateNextEpisodeTimes(series, airdate, simulcastAirdate);
            }

            cursor.moveToNext();
        }
        cursor.close();
        realm.close();
    }

}
