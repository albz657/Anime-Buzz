package me.jakemoritz.animebuzz.data;

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

}
