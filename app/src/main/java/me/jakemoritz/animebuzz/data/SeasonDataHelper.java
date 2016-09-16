package me.jakemoritz.animebuzz.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.HashSet;
import java.util.Set;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonList;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.SeriesList;

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

    public String buildSeasonTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_SEASONS +
                "(" + KEY_SEASON_KEY + " TEXT PRIMARY KEY NOT NULL," +
                KEY_SEASON_NAME + " TEXT," +
                KEY_SEASON_DATE + " TEXT" +
                ")";
    }

    // insertion

    private boolean insertSeasonMetadata(SeasonMetadata metadata) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SEASON_KEY, metadata.getKey());
        contentValues.put(KEY_SEASON_NAME, metadata.getName());
        contentValues.put(KEY_SEASON_DATE, metadata.getStart_timestamp());

        App.getInstance().getDatabase().insert(TABLE_SEASONS, null, contentValues);
        return true;
    }

    public boolean updateSeasonMetadata(SeasonMetadata metadata) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SEASON_KEY, metadata.getKey());
        contentValues.put(KEY_SEASON_NAME, metadata.getName());
        contentValues.put(KEY_SEASON_DATE, metadata.getStart_timestamp());

        App.getInstance().getDatabase().update(TABLE_SEASONS, contentValues, KEY_SEASON_KEY + " =  ? ", new String[]{metadata.getKey()});
        return true;
    }

    public void saveSeasonMetadata(SeasonMetadata metadata) {
        Cursor cursor = getSeasonMetadataCursor(metadata.getKey());
        if (cursor.getCount() != 0) {
            updateSeasonMetadata(metadata);
        } else {
            insertSeasonMetadata(metadata);
        }
        cursor.close();
    }

    // retrieval

    public SeasonList getAllAnimeSeasons() {
        SeasonList seasons = new SeasonList();
        for (SeasonMetadata metadata : App.getInstance().getSeasonsList()) {
            SeriesList tempSeason = AnimeDataHelper.getInstance().getSeriesBySeason(metadata.getName());
            if (tempSeason.size() > 0) {
                seasons.add(new Season(tempSeason, metadata));
            }
        }
        return seasons;
    }

    private Cursor getSeasonMetadataCursor(String seasonKey) {
        return App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_SEASONS + " WHERE " + KEY_SEASON_KEY + " = ?", new String[]{seasonKey});
    }

    public SeasonMetadata getSeasonMetadataFromCursor(Cursor res) {
        String seasonName = res.getString(res.getColumnIndex(KEY_SEASON_NAME));
        String seasonDate = res.getString(res.getColumnIndex(KEY_SEASON_DATE));
        String seasonKey = res.getString(res.getColumnIndex(KEY_SEASON_KEY));

        return new SeasonMetadata(seasonName, seasonDate, seasonKey);
    }

    public Set<SeasonMetadata> getAllSeasonMetadata() {
        Set<SeasonMetadata> seasonList = new HashSet<>();

        Cursor cursor = App.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE_SEASONS, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            seasonList.add(getSeasonMetadataFromCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return seasonList;
    }
}
