package me.jakemoritz.animebuzz.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

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

    // Season metadata columns
    private static final String KEY_SEASON_NAME = "seasonnname";
    private static final String KEY_SEASON_KEY = "seasonkey";
    private static final String KEY_SEASON_DATE = "seasondate";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    private String buildAnimeTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_ANIME +
                "(" + KEY_MALID + " INTEGER PRIMARY KEY NOT NULL," +
                KEY_NAME + " TEXT," +
                KEY_AIRDATE + " INTEGER, " +
                KEY_SIMULCAST_AIRDATE + " INTEGER," +
                KEY_IS_IN_USER_LIST + " INTEGER," +
                KEY_ANIME_SEASON + " TEXT," +
                KEY_CURRENTLY_AIRING + " INTEGER," +
                KEY_ANNID + " INTEGER," +
                KEY_SIMULCAST_DELAY + " DOUBLE," +
                KEY_SIMULCAST + " TEXT" +
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEASONS);
        onCreate(db);
    }

    public boolean insertSeries(Series series) {
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

        db.insert(TABLE_ANIME, null, contentValues);
        return true;
    }

    public boolean insertSeasonMetadata(SeasonMetadata metadata){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SEASON_KEY, metadata.getKey());
        contentValues.put(KEY_SEASON_NAME, metadata.getName());
        contentValues.put(KEY_SEASON_DATE, metadata.getStart_timestamp());

        db.insert(TABLE_SEASONS, null, contentValues);
    }

    public boolean updateSeriesInDb(Series series, String TABLE_NAME) {
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

        db.update(TABLE_NAME, contentValues, KEY_MALID + " = ? ", new String[]{String.valueOf(series.getMALID())});
        return true;
    }

    public Cursor getSeries(int mal_id, String TABLE_NAME) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MALID + " = ?", new String[]{String.valueOf(mal_id)});
    }

    public Cursor getAllSeries(String TABLE_NAME) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Integer deleteSeries(int mal_id, String TABLE_NAME) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME,
                KEY_MALID + " = ? ",
                new String[]{String.valueOf(mal_id)});
    }

    public List<Series> getSeriesBySeason(String TABLE_ANIME, String seasonName){
        List<Series> seriesBySeason = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_ANIME + " WHERE " + KEY_ANIME_SEASON + " ='" + seasonName + "'", null);

        res.moveToFirst();

        for (int i = 0; i < res.getCount(); i++){
            seriesBySeason.add(getSeriesWithCursor(res));
            res.moveToNext();
        }

        return seriesBySeason;
    }

    public void saveSeriesToDb(List<Series> seriesList, String TABLE_NAME) {
        if (seriesList != null) {
            for (Series series : seriesList) {
                if (getSeries(series.getMALID(), TABLE_NAME).getCount() != 0) {
                    updateSeriesInDb(series, TABLE_NAME);
                } else {
                    insertSeries(series, TABLE_NAME);
                }
            }
        }
        close();
    }

    public Series getSeriesWithCursor(Cursor res){
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

        return new Series(airdate, name, MALID, simulcast, simulcast_airdate, season, ANNID, simulcast_delay, isInUserList, isCurrentlyAiring);
    }

    public List<Series> getSeriesUserWatching(String TABLE_ANIME){
        List<Series> userList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_ANIME + " WHERE " + KEY_IS_IN_USER_LIST + " ='" + 1 + "'", null);

        res.moveToFirst();

        for (int i = 0; i < res.getCount(); i++){
            userList.add(getSeriesWithCursor(res));
            res.moveToNext();
        }

        return userList;
    }

    public List<Series> getSeriesFromDb(String TABLE_NAME) {
        Cursor res = getAllSeries(TABLE_NAME);

        res.moveToFirst();
        ArrayList<Series> seriesList = new ArrayList<>();

        for (int i = 0; i < res.getCount(); i++) {
            getSeriesWithCursor(res);
            res.moveToNext();
        }

        close();
        return seriesList;
    }
}
