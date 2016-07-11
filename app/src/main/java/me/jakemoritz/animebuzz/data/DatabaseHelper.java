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

import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private Context context;

    // Database info
    public static final String DATABASE_NAME = "buzzDB";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static String TABLE_ANIME = "TABLE_ANIME";
    private static String TABLE_SEASONS = "TABLE_SEASONS";
    private static String TABLE_ALARMS = "TABLE_ALARMS";

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


    // Season metadata columns
    private static final String KEY_SEASON_NAME = "seasonnname";
    private static final String KEY_SEASON_KEY = "seasonkey";
    private static final String KEY_SEASON_DATE = "seasondate";

    // Alarm columns
    private static final String KEY_ALARM_ID = "alarmid";
    private static final String KEY_ALARM_NAME = "alarmname";
    private static final String KEY_ALARM_TIME = "alarmtime";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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
                KEY_NEXT_EPISODE_SIMULCAST_AIRTIME + " INTEGER" +
                ")";
    }

    private String buildSeasonTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_SEASONS +
                "(" + KEY_SEASON_KEY + " TEXT PRIMARY KEY NOT NULL," +
                KEY_SEASON_NAME + " TEXT," +
                KEY_SEASON_DATE + " TEXT" +
                ")";
    }

    private String buildAlarmTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_ALARMS +
                "(" + KEY_ALARM_ID + " INTEGER PRIMARY KEY NOT NULL," +
                KEY_ALARM_NAME + " TEXT," +
                KEY_ALARM_TIME + " INTEGER" +
                ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(buildAnimeTable());
        db.execSQL(buildSeasonTable());
        db.execSQL(buildAlarmTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEASONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }

    private boolean insertSeries(Series series) {
        SQLiteDatabase db = getWritableDatabase();

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

        db.insert(TABLE_ANIME, null, contentValues);
        return true;
    }

    public boolean insertAlarm(AlarmHolder alarm) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ALARM_NAME, alarm.getSeriesName());
        contentValues.put(KEY_ALARM_TIME, alarm.getAlarmTime());
        contentValues.put(KEY_ALARM_ID, alarm.getId());

        db.insert(TABLE_ALARMS, null, contentValues);
        return true;
    }

    private boolean insertSeasonMetadata(SeasonMetadata metadata) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SEASON_KEY, metadata.getKey());
        contentValues.put(KEY_SEASON_NAME, metadata.getName());
        contentValues.put(KEY_SEASON_DATE, metadata.getStart_timestamp());

        db.insert(TABLE_SEASONS, null, contentValues);
        return true;
    }

    public boolean updateSeasonMetadataInDb(SeasonMetadata metadata) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SEASON_KEY, metadata.getKey());
        contentValues.put(KEY_SEASON_NAME, metadata.getName());
        contentValues.put(KEY_SEASON_DATE, metadata.getStart_timestamp());

        db.update(TABLE_SEASONS, contentValues, KEY_SEASON_KEY + " =  ? ", new String[]{metadata.getKey()});
        return true;
    }

    public boolean updateSeriesInDb(Series series) {
        SQLiteDatabase db = getWritableDatabase();

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

        db.update(TABLE_ANIME, contentValues, KEY_MALID + " = ? ", new String[]{String.valueOf(series.getMALID())});
        return true;
    }

    public Cursor getSeries(int MALID) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ANIME + " WHERE " + KEY_MALID + " = ?", new String[]{String.valueOf(MALID)});
    }

    public Cursor getSeasonMetadata(String seasonKey) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SEASONS + " WHERE " + KEY_SEASON_KEY + " = ?", new String[]{seasonKey});
    }

    public Cursor getAllSeries() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ANIME, null);
    }


    public Cursor getAllSeasonMetadata() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SEASONS, null);
    }

    public Integer deleteSeries(int MALID) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_ANIME,
                KEY_MALID + " = ? ",
                new String[]{String.valueOf(MALID)});
    }

    public Integer deleteAlarm(int id){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_ALARMS, KEY_ALARM_ID + " = ? ", new String[]{String.valueOf(id)});
    }

    public Integer deleteSeasonMetadata(String seasonKey) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_SEASONS,
                KEY_SEASON_KEY + " = ? ",
                new String[]{seasonKey});
    }

    public List<Series> getSeriesBySeason(String seasonName) {
        List<Series> seriesBySeason = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_ANIME + " WHERE " + KEY_ANIME_SEASON + " ='" + seasonName + "'", null);

        res.moveToFirst();

        for (int i = 0; i < res.getCount(); i++) {
            seriesBySeason.add(getSeriesWithCursor(res));
            res.moveToNext();
        }

        res.close();
        return seriesBySeason;
    }

    public void saveSeasonMetadataToDb(SeasonMetadata metadata) {
        Cursor cursor = getSeasonMetadata(metadata.getKey());
        if (cursor.getCount() != 0) {
            updateSeasonMetadataInDb(metadata);
        } else {
            insertSeasonMetadata(metadata);
        }
        cursor.close();
    }

    public void saveAllSeriesToDb(Set<Season> allSeries) {
        List<Series> allSeriesList = new ArrayList<>();
        for (Season season : allSeries) {
            allSeriesList.addAll(season.getSeasonSeries());
        }
        saveSeriesToDb(allSeriesList);
    }

    public void saveSeriesToDb(List<Series> seriesList) {
        Cursor cursor = null;
        for (Series series : seriesList) {
            cursor = getSeries(series.getMALID());
            if (cursor.getCount() != 0) {
                updateSeriesInDb(series);
            } else {
                insertSeries(series);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public List<AlarmHolder> getAllAlarms() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ALARMS, null);

        List<AlarmHolder> alarms = new ArrayList<>();

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++){
            AlarmHolder tempAlarm = getAlarmWithCursor(cursor);
            alarms.add(tempAlarm);
            cursor.moveToNext();
        }

        cursor.close();

        return alarms;
    }

    private AlarmHolder getAlarmWithCursor(Cursor res) {
        int id = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_ALARM_ID));
        long time = (long) res.getLong(res.getColumnIndex(DatabaseHelper.KEY_ALARM_TIME));
        String name = res.getString(res.getColumnIndex(DatabaseHelper.KEY_ALARM_NAME));

        return new AlarmHolder(name, time, id);
    }

    public Series getSeriesWithCursor(Cursor res) {
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

        Type type = new TypeToken<ArrayList<Long>>() {
        }.getType();
        List<Long> backlog = new Gson().fromJson(res.getString(res.getColumnIndex(DatabaseHelper.KEY_BACKLOG)), type);

        return new Series(airdate, name, MALID, simulcast, simulcast_airdate, season, ANNID, simulcast_delay, isInUserList, isCurrentlyAiring, backlog, nextEpisodeAirtime, nextEpisodeSimulcastAirtime);
    }

    public SeasonMetadata getSeasonMetadataWithCursor(Cursor res) {
        String seasonName = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON_NAME));
        String seasonDate = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON_DATE));
        String seasonKey = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON_KEY));

        return new SeasonMetadata(seasonName, seasonDate, seasonKey);
    }

    public Set<Series> getSeriesUserWatching() {
        Set<Series> userList = new HashSet<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_ANIME + " WHERE " + KEY_IS_IN_USER_LIST + " ='" + 1 + "'", null);

        res.moveToFirst();

        for (int i = 0; i < res.getCount(); i++) {
            userList.add(getSeriesWithCursor(res));
            res.moveToNext();
        }

        res.close();
        return userList;
    }

    public List<Series> getSeriesFromDb() {
        Cursor res = getAllSeries();

        res.moveToFirst();
        ArrayList<Series> seriesList = new ArrayList<>();

        for (int i = 0; i < res.getCount(); i++) {
            getSeriesWithCursor(res);
            res.moveToNext();
        }

        res.close();
        return seriesList;
    }
}
