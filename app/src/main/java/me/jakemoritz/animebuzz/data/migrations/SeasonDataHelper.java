package me.jakemoritz.animebuzz.data.migrations;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.realm.Realm;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.models.Season;

public class SeasonDataHelper {

    private static String TABLE_SEASONS = "TABLE_SEASONS";

    // Season metadata columns
    private static final String KEY_SEASON_NAME = "seasonnname";
    private static final String KEY_SEASON_KEY = "seasonkey";
    private static final String KEY_SEASON_DATE = "seasondate";

    private static SeasonDataHelper mInstance;

    public static synchronized SeasonDataHelper getInstance() {
        if (mInstance == null) {
            mInstance = new SeasonDataHelper();
        }
        return mInstance;
    }

    public void migrateSeason(SQLiteDatabase sugarDb) {
        Cursor cursor = sugarDb.rawQuery("SELECT * FROM " + TABLE_SEASONS, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            final String seasonKey = cursor.getString(cursor.getColumnIndex(KEY_SEASON_KEY));
            final String seasonName = cursor.getString(cursor.getColumnIndex(KEY_SEASON_NAME));
            final String startTimeStamp = cursor.getString(cursor.getColumnIndex(KEY_SEASON_DATE));

            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Season season = new Season();
                    season.setKey(seasonKey);
                    season.setName(seasonName);
                    season.setStart_timestamp(startTimeStamp);

                    App.getInstance().getRealm().insertOrUpdate(season);
                }
            });

            cursor.moveToNext();
        }

        final RealmResults<Season> seasonRealmResults = App.getInstance().getRealm().where(Season.class).findAll();
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Season season : seasonRealmResults) {
                    String relativeTime = Season.calculateRelativeTime(season.getName());
                    season.setRelativeTime(relativeTime);
                }
            }
        });

        cursor.close();
    }

}
