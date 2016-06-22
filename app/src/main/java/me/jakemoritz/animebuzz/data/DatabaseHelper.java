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
    private static String TABLE_USER_LIST;
    private static String TABLE_SEASONS;

    // Common columns
    private static final String KEY_MAL_ID = "id";
    private static final String KEY_SHOW_TITLE = "title";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
        TABLE_SEASONS = context.getString(R.string.table_seasons);
        TABLE_USER_LIST = context.getString(R.string.table_user_list);
    }

    private String buildTableCreateQuery(String TABLE_NAME){
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(" + KEY_MAL_ID + " INTEGER PRIMARY KEY NOT NULL, " + KEY_SHOW_TITLE + " TEXT" + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(buildTableCreateQuery(TABLE_SEASONS));
        db.execSQL(buildTableCreateQuery(TABLE_USER_LIST));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEASONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_LIST);
        onCreate(db);
    }

    public boolean insertSeries(Series series, String TABLE_NAME){
        int MAL_ID = series.getMal_id();
        String seriesTitle = series.getTitle();

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_MAL_ID, MAL_ID);
        contentValues.put(KEY_SHOW_TITLE, seriesTitle);

        db.insert(TABLE_NAME , null, contentValues);
        return true;
    }

    public boolean updateSeriesInDb(int mal_id, Series series, String TABLE_NAME){
        int MAL_ID = series.getMal_id();
        String seriesTitle = series.getTitle();

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_MAL_ID, MAL_ID);
        contentValues.put(KEY_SHOW_TITLE, seriesTitle);

        db.update(TABLE_NAME, contentValues, KEY_MAL_ID + " = ? ", new String[]{String.valueOf(MAL_ID)});
        return true;
    }

    public Cursor getSeries(int mal_id, String TABLE_NAME){
        SQLiteDatabase db = getReadableDatabase();
         return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MAL_ID + " = ?", new String[]{String.valueOf(mal_id)});
    }

    public Cursor getAllSeries(String TABLE_NAME){
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Integer deleteTask(int mal_id, String TABLE_NAME){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME,
                KEY_MAL_ID + " = ? ",
                new String[]{String.valueOf(mal_id)});
    }

    public void saveSeriesToDb(ArrayList<Series> seriesList, String TABLE_NAME) {
        if (seriesList != null) {
             for (Series series : seriesList) {
                if (getSeries(series.getMal_id(), TABLE_NAME).getCount() != 0) {
                    updateSeriesInDb(series.getMal_id(), series, TABLE_NAME);
                } else {
                    insertSeries(series, TABLE_NAME);
                }
            }
        }
        close();
    }

    public ArrayList<Series> getSeriesFromDb(String TABLE_NAME) {
        Cursor res = getAllSeries(TABLE_NAME);

        res.moveToFirst();
        ArrayList<Series> seriesList = new ArrayList<>();

        for (int i = res.getCount() - 1; i >= 0; i--) {
            int MAL_ID = res.getInt(res.getColumnIndex(DatabaseHelper.KEY_MAL_ID));
            String seriesTitle = res.getString(res.getColumnIndex(DatabaseHelper.KEY_SHOW_TITLE));

            Series series = new Series(seriesTitle, MAL_ID);
            seriesList.add(series);
            res.moveToNext();
        }

        close();
        return seriesList;
    }
}
