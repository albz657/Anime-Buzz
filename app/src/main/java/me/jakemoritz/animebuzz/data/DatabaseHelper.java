package me.jakemoritz.animebuzz.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "buzzDB";
    private static final int DATABASE_VERSION = 3;
    private boolean isUpgrading = false;

    private static DatabaseHelper mInstance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AnimeDataHelper.getInstance().buildAnimeTable());
        db.execSQL(SeasonDataHelper.getInstance().buildSeasonTable());
        db.execSQL(AlarmsDataHelper.getInstance().buildAlarmTable());
        db.execSQL(BacklogDataHelper.getInstance().buildBacklogTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        isUpgrading = true;
        switch (oldVersion) {
            case 1:
                AnimeDataHelper.getInstance().upgradeDatabaseVersionToVersionTwo(db);
            case 2:
                AlarmsDataHelper.getInstance().upgradeAlarms(db);
                isUpgrading = true;
                AnimeDataHelper.getInstance().upgradeDatabaseVersionToVersionThree(db);
        }
        isUpgrading = false;
    }

    public List<String> getTableColumns(String tableName, SQLiteDatabase database) {
        ArrayList<String> columns = new ArrayList<String>();
        String cmd = "pragma table_info(" + tableName + ");";
        Cursor cur = database.rawQuery(cmd, null);

        while (cur.moveToNext()) {
            columns.add(cur.getString(cur.getColumnIndex("name")));
        }
        cur.close();

        return columns;
    }

    public boolean isUpgrading() {
        return isUpgrading;
    }

    public void setUpgrading(boolean upgrading) {
        isUpgrading = upgrading;
    }
}
