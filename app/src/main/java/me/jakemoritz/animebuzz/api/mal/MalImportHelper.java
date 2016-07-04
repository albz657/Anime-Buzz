package me.jakemoritz.animebuzz.api.mal;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

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

        DatabaseHelper dbHelper = new DatabaseHelper(App.getInstance());
        Cursor res = null;
        for (Integer integer : currentlyWatchingShowIds){
            res = dbHelper.getSeries(integer);
            if (res.getCount() > 0){
                res.moveToFirst();
                Series tempSeries = dbHelper.getSeriesWithCursor(res);
                tempSeries.setInUserList(true);
                matchedSeries.add(tempSeries);
                activity.makeAlarm(tempSeries);
            }
        }

        if (res != null){
            res.close();
        }
        App.getInstance().getUserAnimeList().addAll(matchedSeries);
        App.getInstance().saveAllAnimeSeasonsToDB();
    }
}
