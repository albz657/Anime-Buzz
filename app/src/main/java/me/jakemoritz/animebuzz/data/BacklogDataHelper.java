package me.jakemoritz.animebuzz.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.BacklogItem;
import me.jakemoritz.animebuzz.models.Series;

public class BacklogDataHelper {

    private static String TABLE_BACKLOG = "TABLE_BACKLOG";

    // Backlog columns
    private static final String KEY_BACKLOD_ID = "backlogid";
    private static final String KEY_BACKLOD_MALID = "backlogmalid";
    private static final String KEY_BACKLOG_TIME = "backlogtime";

    private static BacklogDataHelper mInstance;

    public String buildBacklogTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_BACKLOG +
                "(" + KEY_BACKLOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_BACKLOG_TIME + " DOUBLE," +
                KEY_BACKLOD_MALID + " INTEGER" +
                ")";
    }

    public static synchronized BacklogDataHelper getInstance() {
        if (mInstance == null) {
            mInstance = new BacklogDataHelper();
        }
        return mInstance;
    }

    // insertion

    public long insertBacklogItem(BacklogItem backlogItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_BACKLOG_TIME, backlogItem.getAlarmTime());
        contentValues.put(KEY_BACKLOD_MALID, backlogItem.getSeries().getMALID());

        return App.getInstance().getDatabase().insert(TABLE_BACKLOG, null, contentValues);
    }

    // retrieval

    private Cursor getBacklogItemCursor(int id) {
        return App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_BACKLOG + " WHERE " + KEY_BACKLOD_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<BacklogItem> getAllBacklogItems() {
        List<BacklogItem> backlogItems = new ArrayList<>();
        Cursor cursor = App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_BACKLOG, null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BacklogItem backlogItem = getBacklogItemWithCursor(cursor);
            backlogItems.add(backlogItem);
            cursor.moveToNext();
        }

        cursor.close();

        return backlogItems ;
    }

    private BacklogItem getBacklogItemWithCursor(Cursor res) {
        int id = res.getInt(res.getColumnIndex(KEY_BACKLOD_ID));
        long time = res.getLong(res.getColumnIndex(KEY_BACKLOG_TIME));
        int MALID = res.getInt(res.getColumnIndex(KEY_BACKLOD_MALID));

        Series series = AnimeDataHelper.getInstance().getSeries(MALID);

        return new BacklogItem(series, time, id);
    }

    // deletion

    public Integer deleteBacklogItem(int id) {
        return App.getInstance().getDatabase().delete(TABLE_BACKLOG, KEY_BACKLOD_ID + " = ? ", new String[]{String.valueOf(id)});
    }

    public void deleteAllAlarms() {
        App.getInstance().getDatabase().delete(TABLE_BACKLOG, null, null);
    }

    // misc

    public void upgradeToBacklogTable(List<BacklogItem> oldBacklogItems, SQLiteDatabase database){
        database.execSQL(buildBacklogTable());

        for (BacklogItem backlogItem : oldBacklogItems){
            insertBacklogItem(backlogItem);
        }
    }
}
