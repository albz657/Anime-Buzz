package me.jakemoritz.animebuzz.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.models.Series;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    // Database info
    public static final String DATABASE_NAME = "buzzDB";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static String TABLE_ANIME;

    // Common columns
    private static final String KEY_AIRDATE = "airdate";
    private static final String KEY_SHOW_TITLE = "title";
    private static final String KEY_MAL_ID = "_id";
    private static final String KEY_IS_SIMULCAST_AIRED = "issimulcastaired";
    private static final String KEY_IS_AIRED = "isaired";
    private static final String KEY_SIMULCAST_AIRDATE = "simulcastairdate";
    private static final String KEY_IS_IN_USER_LIST = "isinuserlist";
    private static final String KEY_SEASON = "season";
    private static final String KEY_CURRENTLY_AIRING = "iscurrentlyairing";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_ANIME = context.getString(R.string.table_anime);
    }

    private String buildTableCreateQuery(String TABLE_NAME) {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(" + KEY_MAL_ID + " INTEGER PRIMARY KEY NOT NULL," +
                KEY_SHOW_TITLE + " TEXT," +
                KEY_AIRDATE + " INTEGER, " +
                KEY_IS_SIMULCAST_AIRED + " INTEGER," +
                KEY_IS_AIRED + " INTEGER," +
                KEY_SIMULCAST_AIRDATE + " INTEGER," +
                KEY_IS_IN_USER_LIST + " INTEGER," +
                KEY_SEASON + " TEXT," +
                KEY_CURRENTLY_AIRING + " INTEGER" +
                ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(buildTableCreateQuery(TABLE_ANIME));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIME);
        onCreate(db);
    }

    public boolean insertSeries(Series series, String TABLE_NAME) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_AIRDATE, series.getAirdate());
        contentValues.put(KEY_SHOW_TITLE, series.getTitle());
        contentValues.put(KEY_MAL_ID, series.getMal_id());
        contentValues.put(KEY_IS_SIMULCAST_AIRED, series.isSimulcastAired() ? 1 : 0);
        contentValues.put(KEY_IS_AIRED, series.isAired() ? 1 : 0);
        contentValues.put(KEY_SIMULCAST_AIRDATE, series.getSimulcast_airdate());
        contentValues.put(KEY_IS_IN_USER_LIST, series.isInUserList() ? 1 : 0);
        contentValues.put(KEY_SEASON, series.getSeason());
        contentValues.put(KEY_CURRENTLY_AIRING, series.isCurrentlyAiring() ? 1 : 0);

        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updateSeriesInDb(Series series, String TABLE_NAME) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_AIRDATE, series.getAirdate());
        contentValues.put(KEY_SHOW_TITLE, series.getTitle());
        contentValues.put(KEY_MAL_ID, series.getMal_id());
        contentValues.put(KEY_IS_SIMULCAST_AIRED, series.isSimulcastAired() ? 1 : 0);
        contentValues.put(KEY_IS_AIRED, series.isAired() ? 1 : 0);
        contentValues.put(KEY_SIMULCAST_AIRDATE, series.getSimulcast_airdate());
        contentValues.put(KEY_IS_IN_USER_LIST, series.isInUserList() ? 1 : 0);
        contentValues.put(KEY_SEASON, series.getSeason());
        contentValues.put(KEY_CURRENTLY_AIRING, series.isCurrentlyAiring() ? 1 : 0);

        db.update(TABLE_NAME, contentValues, KEY_MAL_ID + " = ? ", new String[]{String.valueOf(series.getMal_id())});
        return true;
    }

    public Cursor getSeries(int mal_id, String TABLE_NAME) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MAL_ID + " = ?", new String[]{String.valueOf(mal_id)});
    }

    public Cursor getAllSeries(String TABLE_NAME) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Integer deleteTask(int mal_id, String TABLE_NAME) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME,
                KEY_MAL_ID + " = ? ",
                new String[]{String.valueOf(mal_id)});
    }

    public void saveSeriesToDb(ArrayList<Series> seriesList, String TABLE_NAME) {
        if (seriesList != null) {
            for (Series series : seriesList) {
                if (getSeries(series.getMal_id(), TABLE_NAME).getCount() != 0) {
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
        String seriesTitle = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SHOW_TITLE));
        int MAL_ID = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_MAL_ID));
        boolean isSimulcastAired = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_IS_SIMULCAST_AIRED)) == 1);
        boolean isAired = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_IS_AIRED)) == 1);
        int simulcastAirdate = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_SIMULCAST_AIRDATE));
        boolean isInUserList = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_IS_IN_USER_LIST)) == 1);
        String season = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON));
        boolean isCurrentlyAiring = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_CURRENTLY_AIRING)) == 1);

        Series series = new Series(airdate, seriesTitle, MAL_ID, isSimulcastAired, isAired, simulcastAirdate, isInUserList, season, isCurrentlyAiring);
        return series;
    }

    public ArrayList<Series> getSeriesFromDb(String TABLE_NAME) {
        Cursor res = getAllSeries(TABLE_NAME);

        res.moveToFirst();
        ArrayList<Series> seriesList = new ArrayList<>();

        for (int i = 0; i < res.getCount(); i++) {
            int airdate = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_AIRDATE));
            String seriesTitle = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SHOW_TITLE));
            int MAL_ID = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_MAL_ID));
            boolean isSimulcastAired = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_IS_SIMULCAST_AIRED)) == 1);
            boolean isAired = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_IS_AIRED)) == 1);
            int simulcastAirdate = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_SIMULCAST_AIRDATE));
            boolean isInUserList = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_IS_IN_USER_LIST)) == 1);
            String season = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SEASON));
            boolean isCurrentlyAiring = (res.getInt(res.getColumnIndex(DatabaseHelper.KEY_CURRENTLY_AIRING)) == 1);

            Series series = new Series(airdate, seriesTitle, MAL_ID, isSimulcastAired, isAired, simulcastAirdate, isInUserList, season, isCurrentlyAiring);
            seriesList.add(series);
            res.moveToNext();
        }

        close();
        return seriesList;
    }
}
