package me.jakemoritz.animebuzz.api.mal;

import android.database.Cursor;

import java.util.List;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.MalDataRead;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class MalImportHelper {

    private MainActivity activity;
    private MalDataRead delegate;
    private SeriesFragment fragment;

    public MalImportHelper(SeriesFragment seriesFragment, MalDataRead delegate) {
        this.activity = (MainActivity) seriesFragment.activity;
        this.fragment = seriesFragment;
        this.delegate = delegate;
    }

    public void matchSeries(List<Integer> currentlyWatchingShowIds){
        SeriesList matchedSeries = new SeriesList();

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(App.getInstance());
        Cursor res = null;
        for (Integer integer : currentlyWatchingShowIds){
            res = dbHelper.getSeries(integer);
            if (res.getCount() > 0){
                res.moveToFirst();
                Series tempSeries = dbHelper.getSeriesWithCursor(res);
                tempSeries.setInUserList(true);
                matchedSeries.add(tempSeries);

                if (tempSeries.getAirdate() > 0 && tempSeries.getSimulcast_airdate() > 0) {
//                    App.getInstance().makeAlarm(tempSeries);
                }
            }
        }

        if (res != null){
            res.close();
        }

        App.getInstance().getUserAnimeList().addAll(matchedSeries);

        if (fragment instanceof MyShowsFragment){
            ((MyShowsFragment) fragment).loadUserSortingPreference();
        } else {
            fragment.mAdapter.getVisibleSeries().clear();
            fragment.mAdapter.getVisibleSeries().addAll(fragment.mAdapter.getAllSeries());
        }
        App.getInstance().saveUserListToDB();
        delegate.malDataRead();
    }
}
