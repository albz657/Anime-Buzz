package me.jakemoritz.animebuzz.api.mal;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.MalDataRead;
import me.jakemoritz.animebuzz.models.Series;

public class MalImportHelper {

    private MainActivity activity;
    private MalDataRead delegate;
    private SeriesFragment fragment;

    public MalImportHelper(SeriesFragment seriesFragment, MalDataRead delegate) {
        this.activity = (MainActivity) seriesFragment.getActivity();
        this.fragment = seriesFragment;
        this.delegate = delegate;
    }

    public void matchSeries(List<Integer> currentlyWatchingShowIds){
        List<Series> matchedSeries = new ArrayList<>();

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
        fragment.mAdapter.getAllSeries().clear();
        fragment.mAdapter.getAllSeries().addAll(matchedSeries);
        fragment.mAdapter.getVisibleSeries().clear();
        fragment.mAdapter.getVisibleSeries().addAll(fragment.mAdapter.getAllSeries());
        App.getInstance().saveAllAnimeSeasonsToDB();
        delegate.malDataRead();
    }
}
