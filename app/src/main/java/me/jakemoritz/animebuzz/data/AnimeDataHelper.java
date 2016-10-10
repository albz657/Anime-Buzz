package me.jakemoritz.animebuzz.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.models.BacklogItem;

public class AnimeDataHelper {

    private static final String TAG = AnimeDataHelper.class.getSimpleName();

    private static String TABLE_ANIME = "TABLE_ANIME";

    // Anime columns
    private static final String KEY_AIRDATE = "airdate";
    private static final String KEY_NAME = "title";
    private static final String KEY_MALID = "_id";
    private static final String KEY_SIMULCAST_AIRDATE = "simulcastairdate";
    private static final String KEY_IS_IN_USER_LIST = "isinuserlist";
    private static final String KEY_ANIME_SEASON = "animeseason";
    private static final String KEY_CURRENTLY_AIRING = "iscurrentlyairing";
    private static final String KEY_ANNID = "ANNID";
    private static final String KEY_SIMULCAST_DELAY = "simulcastdelay";
    private static final String KEY_SIMULCAST = "simulcast";
    private static final String KEY_BACKLOG = "backlog";
    private static final String KEY_NEXT_EPISODE_AIRTIME = "nextepisodeairtime";
    private static final String KEY_NEXT_EPISODE_SIMULCAST_AIRTIME = "nextepisodesimulcastairtime";
    private static final String KEY_EPISODES_WATCHED = "episodeswatched";
    private static final String KEY_NEXT_EPISODE_AIRTIME_FORMATTED = "nextepisodeairtimeformatted";
    private static final String KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED = "nextepisodesimulcastairtimeformatted";
    private static final String KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24 = "nextepisodeairtimeformatted_twofour";
    private static final String KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24 = "nextepisodesimulcastairtimeformatted_twofour";

    private static AnimeDataHelper mInstance;

    private List<BacklogItem> oldBacklogItems;

    public static synchronized AnimeDataHelper getInstance() {
        if (mInstance == null) {
            mInstance = new AnimeDataHelper();
        }
        return mInstance;
    }

    // retrieval

/*    private Series getSeriesFromCursor(Cursor res) {
        int airdate = res.getInt(res.getColumnIndex(KEY_AIRDATE));
        String name = res.getString(res.getColumnIndex(KEY_NAME));
        int MALID = res.getInt(res.getColumnIndex(KEY_MALID));
        int simulcast_airdate = res.getInt(res.getColumnIndex(KEY_SIMULCAST_AIRDATE));
        boolean isInUserList = (res.getInt(res.getColumnIndex(KEY_IS_IN_USER_LIST)) == 1);
        String season = res.getString(res.getColumnIndex(KEY_ANIME_SEASON));
        boolean isCurrentlyAiring = (res.getInt(res.getColumnIndex(KEY_CURRENTLY_AIRING)) == 1);
        int ANNID = res.getInt(res.getColumnIndex(KEY_ANNID));
        double simulcast_delay = res.getDouble(res.getColumnIndex(KEY_SIMULCAST_DELAY));
        String simulcast = res.getString(res.getColumnIndex(KEY_SIMULCAST));
        long nextEpisodeAirtime = res.getLong(res.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME));
        long nextEpisodeSimulcastAirtime = res.getLong(res.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME));
        int episodesWatched = res.getInt(res.getColumnIndex(KEY_EPISODES_WATCHED));
        String nextEpisodeAirtimeFormatted = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME_FORMATTED));
        String nextEpisodeSimulcastAirtimeFormatted = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED));
        String nextEpisodeAirtimeFormatted24 = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24));
        String nextEpisodeSimulcastAirtimeFormatted24 = res.getString(res.getColumnIndex(KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24));

        Series series = new Series(airdate, name, (long) MALID, simulcast, simulcast_airdate, season, ANNID, simulcast_delay, isInUserList, isCurrentlyAiring, nextEpisodeAirtime, nextEpisodeSimulcastAirtime, episodesWatched, nextEpisodeAirtimeFormatted, nextEpisodeSimulcastAirtimeFormatted, nextEpisodeAirtimeFormatted24, nextEpisodeSimulcastAirtimeFormatted24, 0, "", "", "", false, "", "", false);

        Type type = new TypeToken<ArrayList<Long>>() {
        }.getType();
        List<Long> backlog = new Gson().fromJson(res.getString(res.getColumnIndex(KEY_BACKLOG)), type);

        for (long backlogTime : backlog){
            oldBacklogItems.add(new BacklogItem(series, backlogTime));
        }

        return null;
    }

    private SeriesList getAllSeries(SQLiteDatabase database) {
        SeriesList allSeries = new SeriesList();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ANIME, null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Series series = getSeriesFromCursor(cursor);
            allSeries.add(series);
            cursor.moveToNext();
        }

        cursor.close();

        return allSeries;
    }*/

    // misc

    void upgradeDatabaseVersionToVersionTwo(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE " + TABLE_ANIME + " ADD COLUMN " + KEY_NEXT_EPISODE_AIRTIME_FORMATTED_24 + " TEXT");
        database.execSQL("ALTER TABLE " + TABLE_ANIME + " ADD COLUMN " + KEY_NEXT_EPISODE_SIMULCAST_AIRTIME_FORMATTED_24 + " TEXT");
    }

    void upgradeDatabaseVersionToVersionThree(SQLiteDatabase database) {
        oldBacklogItems = new ArrayList<>();

//        SeriesList allSeries = getAllSeries(database);

//        Series.saveInTx(allSeries);

//        BacklogItem.saveInTx(oldBacklogItems);
    }
}
