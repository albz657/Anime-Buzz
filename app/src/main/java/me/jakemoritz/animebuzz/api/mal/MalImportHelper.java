package me.jakemoritz.animebuzz.api.mal;

import android.database.Cursor;

import java.util.List;

import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
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

    public void matchSeries(List<MatchHolder> matchList) {
        SeriesList matchedSeries = new SeriesList();

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(App.getInstance());
        Cursor res = null;
        for (MatchHolder match : matchList) {
            Series tempSeries = dbHelper.getSeries(match.getMALID());

            if (tempSeries != null) {
                tempSeries.setInUserList(true);
                tempSeries.setEpisodesWatched(match.getEpisodesWatched());
                matchedSeries.add(tempSeries);

//                App.getInstance().getCircleBitmap(tempSeries);

                if (tempSeries.getAirdate() > 0 && tempSeries.getSimulcast_airdate() > 0) {
                    App.getInstance().makeAlarm(tempSeries);
                }
            }
        }

        App.getInstance().getUserAnimeList().addAll(matchedSeries);

        if (fragment.mAdapter != null) {
            fragment.mAdapter.getVisibleSeries().clear();
            fragment.mAdapter.getVisibleSeries().addAll(App.getInstance().getUserAnimeList());
        }


        if (fragment instanceof MyShowsFragment) {
            ((MyShowsFragment) fragment).loadUserSortingPreference();
        }

        dbHelper.saveSeriesList(App.getInstance().getUserAnimeList());
        delegate.malDataRead();
    }
}
