package me.jakemoritz.animebuzz.mal_api;

import android.database.Cursor;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.Series;

public class MalImportHelper {

    private MainActivity activity;

    public MalImportHelper(MainActivity activity) {
        this.activity = activity;
    }

    public void matchSeries(List<Integer> currentlyWatchingShowIds){
        ArrayList<Series> matchedSeries = new ArrayList<>();

        for (int i = 0; i < App.getInstance().getAllAnimeList().size(); i++){
            for (int j = 0; j < currentlyWatchingShowIds.size(); j++){
                Series tempSeries = App.getInstance().getAllAnimeList().get(i);
                if (currentlyWatchingShowIds.get(j) == tempSeries.getMALID()){
                    // found a match in current season data
                    tempSeries.setInUserList(true);
                    matchedSeries.add(tempSeries);

                    activity.makeAlarm(tempSeries);
                }
            }
        }

        App.getInstance().getUserAnimeList().clear();
        App.getInstance().getUserAnimeList().addAll(matchedSeries);
        App.getInstance().saveAnimeListToDB();

    }

    public void importPoster(Bitmap poster, String MALID){
        DatabaseHelper helper = new DatabaseHelper(activity);
        Cursor cursor = helper.getSeries(Integer.valueOf(MALID), activity.getString(R.string.table_anime));
        cursor.moveToFirst();
        Series series = helper.getSeriesWithCursor(cursor);
        series.setPoster(poster);

        ArrayList<Series> seriesHolder = new ArrayList<>();
        seriesHolder.add(series);
        App.getInstance().saveUserAnimeList(seriesHolder);
    }

}
