package me.jakemoritz.animebuzz.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class AnimeDataHelper {

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

    public static synchronized AnimeDataHelper getInstance() {
        if (mInstance == null) {
            mInstance = new AnimeDataHelper();
        }
        return mInstance;
    }

    public String buildAnimeTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_ANIME +
                "(" + KEY_MALID + " INTEGER PRIMARY KEY NOT NULL," +
                KEY_NAME + " TEXT," +
                KEY_AIRDATE + " INTEGER," +
                KEY_SIMULCAST_AIRDATE + " INTEGER," +
                KEY_IS_IN_USER_LIST + " INTEGER," +
                KEY_ANIME_SEASON + " TEXT," +
                KEY_CURRENTLY_AIRING + " INTEGER," +
                KEY_ANNID + " INTEGER," +
                KEY_SIMULCAST_DELAY + " DOUBLE," +
                KEY_SIMULCAST + " TEXT," +
                KEY_BACKLOG + " TEXT," +
                KEY_NEXT_EPISODE_AIRTIME + " INTEGER," +
                KEY_NEXT_EPISODE_SIMULCAST_AIRTIME + " INTEGER, " +
                KEY_EPISODES_WATCHED + " INTEGER, " +
                KEY_NEXT_EPISODE_AIRTIME_FORMATTED + " TEXT, " +
                KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED + " TEXT, " +
                KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24 + " TEXT, " +
                KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24 + " TEXT" +
                ")";
    }

    // insertion

    private boolean insertSeries(Series series) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_AIRDATE, series.getAirdate());
        contentValues.put(KEY_NAME, series.getName());
        contentValues.put(KEY_MALID, series.getMALID());
        contentValues.put(KEY_SIMULCAST_AIRDATE, series.getSimulcast_airdate());
        contentValues.put(KEY_IS_IN_USER_LIST, series.isInUserList() ? 1 : 0);
        contentValues.put(KEY_ANIME_SEASON, series.getSeason());
        contentValues.put(KEY_CURRENTLY_AIRING, series.isCurrentlyAiring() ? 1 : 0);
        contentValues.put(KEY_ANNID, series.getANNID());
        contentValues.put(KEY_SIMULCAST, series.getSimulcast());
        contentValues.put(KEY_SIMULCAST_DELAY, series.getSimulcast_delay());
        contentValues.put(KEY_NEXT_EPISODE_AIRTIME, series.getNextEpisodeAirtime());
        contentValues.put(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME, series.getNextEpisodeSimulcastTime());
        contentValues.put(KEY_EPISODES_WATCHED, series.getEpisodesWatched());
        contentValues.put(KEY_NEXT_EPISODE_AIRTIME_FORMATTED, series.getNextEpisodeAirtimeFormatted());
        contentValues.put(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED, series.getNextEpisodeSimulcastTimeFormatted());
        contentValues.put(KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24, series.getNextEpisodeAirtimeFormatted24());
        contentValues.put(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24, series.getNextEpisodeSimulcastTimeFormatted24());

        App.getInstance().getDatabase().insert(TABLE_ANIME, null, contentValues);
        return true;
    }

    private boolean updateSeries(Series series) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_AIRDATE, series.getAirdate());
        contentValues.put(KEY_NAME, series.getName());
        contentValues.put(KEY_MALID, series.getMALID());
        contentValues.put(KEY_SIMULCAST_AIRDATE, series.getSimulcast_airdate());
        contentValues.put(KEY_IS_IN_USER_LIST, series.isInUserList() ? 1 : 0);
        contentValues.put(KEY_ANIME_SEASON, series.getSeason());
        contentValues.put(KEY_CURRENTLY_AIRING, series.isCurrentlyAiring() ? 1 : 0);
        contentValues.put(KEY_ANNID, series.getANNID());
        contentValues.put(KEY_SIMULCAST, series.getSimulcast());
        contentValues.put(KEY_SIMULCAST_DELAY, series.getSimulcast_delay());
        contentValues.put(KEY_NEXT_EPISODE_AIRTIME, series.getNextEpisodeAirtime());
        contentValues.put(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME, series.getNextEpisodeSimulcastTime());
        contentValues.put(KEY_EPISODES_WATCHED, series.getEpisodesWatched());
        contentValues.put(KEY_NEXT_EPISODE_AIRTIME_FORMATTED, series.getNextEpisodeAirtimeFormatted());
        contentValues.put(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED, series.getNextEpisodeSimulcastTimeFormatted());
        contentValues.put(KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24, series.getNextEpisodeAirtimeFormatted24());
        contentValues.put(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24, series.getNextEpisodeSimulcastTimeFormatted24());

        App.getInstance().getDatabase().update(TABLE_ANIME, contentValues, KEY_MALID + " = ? ", new String[]{String.valueOf(series.getMALID())});
        return true;
    }

    public void saveSeriesList(SeriesList seriesList, SQLiteDatabase database) {
        Cursor cursor = null;

        for (Series series : seriesList) {
            cursor = getSeriesCursor(series.getMALID());
            if (cursor.getCount() != 0) {
                updateSeries(series);
            } else {
                insertSeries(series);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    // retrieval

    public Series getSeries(int MALID) {
        Cursor cursor = getSeriesCursor(MALID);
        Series series = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            series = getSeriesFromCursor(cursor);
        }
        cursor.close();

        return series;
    }

    private Cursor getSeriesCursor(int MALID) {
        return App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_ANIME + " WHERE " + KEY_MALID + " = ?", new String[]{String.valueOf(MALID)});
    }

    public SeriesList getSeriesBySeason(String seasonName) {
        SeriesList seriesBySeason = new SeriesList();

        Cursor res = App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_ANIME + " WHERE " + KEY_ANIME_SEASON + " ='" + seasonName + "'", null);

        res.moveToFirst();

        for (int i = 0; i < res.getCount(); i++) {
            seriesBySeason.add(getSeriesFromCursor(res));
            res.moveToNext();
        }

        res.close();
        return seriesBySeason;
    }

    private Series getSeriesFromCursor(Cursor res) {
        int airdate = res.getInt(res.getColumnIndex(KEY_AIRDATE));
        String name = res.getString(res.getColumnIndex(KEY_NAME));
        int MALID = res.getInt(res.getColumnIndex(KEY_MALID));
        int simulcast_airdate = res.getInt(res.getColumnIndex(KEY_SIMULCAST_AIRDATE));
        boolean isInUserList = (res.getInt(res.getColumnIndex(KEY_IS_IN_USER_LIST)) == 1);
        String season = res.getString(res.getColumnIndex(KEY_ANIME_SEASON));
        boolean isCurrentlyAiring = (res.getInt(res.getColumnIndex(KEY_CURRENTLY_AIRING)) == 1);
        int ANNID = res.getInt(res.getColumnIndex(KEY_ANNID));
        double simulcast_delay = res.getDouble(res.getColumnIndex(KEY_SIMULCAST_DELAY));
        String simulcast = res.getString(res.getColumnIndex(KEY_SIMULCAST));
        long nextEpisodeAirtime = res.getLong(res.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME));
        long nextEpisodeSimulcastAirtime = res.getLong(res.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME));
        int episodesWatched = res.getInt(res.getColumnIndex(KEY_EPISODES_WATCHED));
        String nextEpisodeAirtimeFormatted = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME_FORMATTED));
        String nextEpisodeSimulcastAirtimeFormatted = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED));
        String nextEpisodeAirtimeFormatted24 = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24));
        String nextEpisodeSimulcastAirtimeFormatted24 = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24));

        Series series = new Series(airdate, name, MALID, simulcast, simulcast_airdate, season, ANNID, simulcast_delay, isInUserList, isCurrentlyAiring, nextEpisodeAirtime, nextEpisodeSimulcastAirtime, episodesWatched, nextEpisodeAirtimeFormatted, nextEpisodeSimulcastAirtimeFormatted, nextEpisodeAirtimeFormatted24, nextEpisodeSimulcastAirtimeFormatted24);

        if (DatabaseHelper.getInstance(App.getInstance()).isUpgrading()){
            Type type = new TypeToken<ArrayList<Long>>() {
            }.getType();
            List<Long> backlog = new Gson().fromJson(res.getString(res.getColumnIndex(KEY_BACKLOG)), type);

            series.setBacklog(backlog);
        }

        return series;
    }

    public SeriesList getAllSeries(SQLiteDatabase database) {
        SeriesList allSeries = new SeriesList();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ANIME, null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Series series = getSeriesFromCursor(cursor);
            allSeries.add(series);
            cursor.moveToNext();
        }

        cursor.close();

        return allSeries;
    }

    // misc

    public void upgradeDatabaseVersionToVersionTwo(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE " + TABLE_ANIME + " ADD COLUMN " + KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24 + " TEXT");
        database.execSQL("ALTER TABLE " + TABLE_ANIME + " ADD COLUMN " + KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24 + " TEXT");
    }

    public void upgradeDatabaseVersionToVersionThree(SQLiteDatabase database) {
        SeriesList allSeries = getAllSeries(database);

        List<BacklogItem> oldBacklogItems = new ArrayList<>();

        for (Series series : allSeries) {
            if (!series.getBacklog().isEmpty()) {
                for (Long backlogTime : series.getBacklog()) {
                    oldBacklogItems.add(new BacklogItem(series, backlogTime));
                }
            }
        }

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIME);
        database.execSQL(buildAnimeTable());

        saveSeriesList(allSeries, database);

        BacklogDataHelper.getInstance().upgradeToBacklogTable(oldBacklogItems, database);
    }
}
