package me.jakemoritz.animebuzz.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.Set;

import me.jakemoritz.animebuzz.models.SeasonMetadata;

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

    // retrieval

    private SeasonMetadata getSeasonMetadataFromCursor(Cursor res) {
        String seasonName = res.getString(res.getColumnIndex(KEY_SEASON_NAME));
        String seasonDate = res.getString(res.getColumnIndex(KEY_SEASON_DATE));
        String seasonKey = res.getString(res.getColumnIndex(KEY_SEASON_KEY));

        return new SeasonMetadata(seasonName, seasonDate, seasonKey);
    }

    public Set<SeasonMetadata> getAllSeasonMetadata(SQLiteDatabase database) {
        Set<SeasonMetadata> seasonList = new HashSet<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_SEASONS, null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            seasonList.add(getSeasonMetadataFromCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return seasonList;
    }

    void upgradeToSugar(SQLiteDatabase database){
        Set<SeasonMetadata> seasonMetadataList = getAllSeasonMetadata(database);

        SeasonMetadata.saveInTx(seasonMetadataList);
    }
}
