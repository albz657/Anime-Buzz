package me.jakemoritz.animebuzz.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                AnimeDataHelper.getInstance().upgradeDatabaseVersionToVersionTwo(db);
            case 2:
                AlarmsDataHelper.getInstance().upgradeAlarms(db);
                AnimeDataHelper.getInstance().upgradeDatabaseVersionToVersionThree(db);
                SeasonDataHelper.getInstance().upgradeToSugar(db);
        }
    }
}
