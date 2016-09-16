package me.jakemoritz.animebuzz.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonList;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private Context context;

    // Database info
    public static final String DATABASE_NAME = "buzzDB";
    private static final int DATABASE_VERSION = 3;

    // Table names
    private static String TABLE_ANIME = "TABLE_ANIME";
    private static String TABLE_SEASONS = "TABLE_SEASONS";

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

    // Season metadata columns
    private static final String KEY_SEASON_NAME = "seasonnname";
    private static final String KEY_SEASON_KEY = "seasonkey";
    private static final String KEY_SEASON_DATE = "seasondate";

    private static DatabaseHelper mInstance;

    private boolean isUpgrading = false;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    public boolean isUpgrading() {
        return isUpgrading;
    }

    public void setUpgrading(boolean upgrading) {
        isUpgrading = upgrading;
    }

    private String buildAnimeTable() {
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

    private String buildSeasonTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_SEASONS +
                "(" + KEY_SEASON_KEY + " TEXT PRIMARY KEY NOT NULL," +
                KEY_SEASON_NAME + " TEXT," +
                KEY_SEASON_DATE + " TEXT" +
                ")";
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(buildAnimeTable());
        db.execSQL(buildSeasonTable());
        db.execSQL(AlarmsDataHelper.getInstance().buildAlarmTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        isUpgrading = true;
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + TABLE_ANIME + " ADD COLUMN " + KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24 + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_ANIME + " ADD COLUMN " + KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24 + " TEXT");
            case 2:
                AlarmsDataHelper.getInstance().upgradeAlarms(db);

        }
        isUpgrading = false;
    }


    //    Insert/Update

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
        contentValues.put(KEY_BACKLOG, new Gson().toJson(series.getBacklog()));
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
        contentValues.put(KEY_BACKLOG, new Gson().toJson(series.getBacklog()));
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

    private boolean insertSeasonMetadata(SeasonMetadata metadata) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SEASON_KEY, metadata.getKey());
        contentValues.put(KEY_SEASON_NAME, metadata.getName());
        contentValues.put(KEY_SEASON_DATE, metadata.getStart_timestamp());

        App.getInstance().getDatabase().insert(TABLE_SEASONS, null, contentValues);
        return true;
    }

    public boolean updateSeasonMetadata(SeasonMetadata metadata) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SEASON_KEY, metadata.getKey());
        contentValues.put(KEY_SEASON_NAME, metadata.getName());
        contentValues.put(KEY_SEASON_DATE, metadata.getStart_timestamp());

        App.getInstance().getDatabase().update(TABLE_SEASONS, contentValues, KEY_SEASON_KEY + " =  ? ", new String[]{metadata.getKey()});
        return true;
    }

//    Get

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

    private SeriesList getSeriesBySeason(String seasonName) {
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
        int airdate = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_AIRDATE));
        String name = res.getString(res.getColumnIndex(DatabaseHelper.KEY_NAME));
        int MALID = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_MALID));
        int simulcast_airdate = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_SIMULCAST_AIRDATE));
        boolean isInUserList = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_IS_IN_USER_LIST)) == 1);
        String season = res.getString(res.getColumnIndex(DatabaseHelper.KEY_ANIME_SEASON));
        boolean isCurrentlyAiring = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_CURRENTLY_AIRING)) == 1);
        int ANNID = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_ANNID));
        double simulcast_delay = res.getDouble(res.getColumnIndex(DatabaseHelper.KEY_SIMULCAST_DELAY));
        String simulcast = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SIMULCAST));
        long nextEpisodeAirtime = res.getLong(res.getColumnIndex(DatabaseHelper.KEY_NEXT_EPISODE_AIRTIME));
        long nextEpisodeSimulcastAirtime = res.getLong(res.getColumnIndex(DatabaseHelper.KEY_NEXT_EPISODE_SIMULCAST_AIRTIME));
        int episodesWatched = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_EPISODES_WATCHED));
        String nextEpisodeAirtimeFormatted = res.getString(res.getColumnIndex(DatabaseHelper.KEY_NEXT_EPISODE_AIRTIME_FORMATTED));
        String nextEpisodeSimulcastAirtimeFormatted = res.getString(res.getColumnIndex(DatabaseHelper.KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED));
        String nextEpisodeAirtimeFormatted24 = res.getString(res.getColumnIndex(DatabaseHelper.KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24));
        String nextEpisodeSimulcastAirtimeFormatted24 = res.getString(res.getColumnIndex(DatabaseHelper.KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24));

        Type type = new TypeToken<ArrayList<Long>>() {
        }.getType();
        List<Long> backlog = new Gson().fromJson(res.getString(res.getColumnIndex(DatabaseHelper.KEY_BACKLOG)), type);

        return new Series(airdate, name, MALID, simulcast, simulcast_airdate, season, ANNID, simulcast_delay, isInUserList, isCurrentlyAiring, backlog, nextEpisodeAirtime, nextEpisodeSimulcastAirtime, episodesWatched, nextEpisodeAirtimeFormatted, nextEpisodeSimulcastAirtimeFormatted, nextEpisodeAirtimeFormatted24, nextEpisodeSimulcastAirtimeFormatted24);
    }

    public SeasonList getAllAnimeSeasons() {
        SeasonList seasons = new SeasonList();
        for (SeasonMetadata metadata : App.getInstance().getSeasonsList()) {
            SeriesList tempSeason = getSeriesBySeason(metadata.getName());
            if (tempSeason.size() > 0) {
                seasons.add(new Season(tempSeason, metadata));
            }
        }
        return seasons;
    }

    private Cursor getSeasonMetadataCursor(String seasonKey) {
        return App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_SEASONS + " WHERE " + KEY_SEASON_KEY + " = ?", new String[]{seasonKey});
    }

    public SeasonMetadata getSeasonMetadataFromCursor(Cursor res) {
        String seasonName = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON_NAME));
        String seasonDate = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON_DATE));
        String seasonKey = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON_KEY));

        return new SeasonMetadata(seasonName, seasonDate, seasonKey);
    }

    public Set<SeasonMetadata> getAllSeasonMetadata() {
        Set<SeasonMetadata> seasonList = new HashSet<>();

        Cursor cursor = App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_SEASONS, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            seasonList.add(getSeasonMetadataFromCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return seasonList;
    }

//    Public saving

    public void saveSeasonMetadata(SeasonMetadata metadata) {
        Cursor cursor = getSeasonMetadataCursor(metadata.getKey());
        if (cursor.getCount() != 0) {
            updateSeasonMetadata(metadata);
        } else {
            insertSeasonMetadata(metadata);
        }
        cursor.close();
    }

    public void saveSeriesList(SeriesList seriesList) {
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
}
