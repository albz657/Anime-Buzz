package me.jakemoritz.animebuzz.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.jakemoritz.animebuzz.helpers.App;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "buzzDB";
    private static final int DATABASE_VERSION = 3;

    private static DatabaseHelper mInstance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    public void migrateToRealm(){
        SQLiteDatabase db = SQLiteDatabase.openDatabase(App.getInstance().getDatabasePath(DATABASE_NAME).getPath(), null, 0);

        SeasonDataHelper.getInstance().migrateSeason(db);
        AnimeDataHelper.getInstance().migrateSeries(db);
        AlarmsDataHelper.getInstance().migrateAlarms(db);
    }
}
