package me.jakemoritz.animebuzz.mal_api;

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
        Cursor res;
        for (Integer integer : currentlyWatchingShowIds){
            res = dbHelper.getSeries(integer);
            res.moveToFirst();
            Series tempSeries = dbHelper.getSeriesWithCursor(res);
            tempSeries.setInUserList(true);
            matchedSeries.add(tempSeries);
            activity.makeAlarm(tempSeries);
        }

        App.getInstance().getUserAnimeList().addAll(matchedSeries);
        App.getInstance().saveAllAnimeSeasonsToDB();
    }
}
