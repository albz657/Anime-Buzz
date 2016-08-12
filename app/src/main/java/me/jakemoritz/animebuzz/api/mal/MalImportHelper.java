package me.jakemoritz.animebuzz.api.mal;

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

        for (MatchHolder match : matchList) {
            Series tempSeries = dbHelper.getSeries(match.getMALID());

            if (tempSeries != null) {
                tempSeries.setInUserList(true);
                tempSeries.setEpisodesWatched(match.getEpisodesWatched());
                matchedSeries.add(tempSeries);

//                App.getInstance().getCircleBitmap(tempSeries);
            }
        }

        App.getInstance().getUserAnimeList().addAll(matchedSeries);
        for (Series series : App.getInstance().getUserAnimeList()){
            if (series.getAirdate() > 0 && series.getSimulcast_airdate() > 0) {
                App.getInstance().makeAlarm(series);
            }
        }

        if (fragment.mAdapter != null) {
            fragment.mAdapter.setVisibleSeries((SeriesList) App.getInstance().getUserAnimeList().clone());
        }


        if (fragment instanceof MyShowsFragment) {
            ((MyShowsFragment) fragment).loadUserSortingPreference();
        }

        dbHelper.saveSeriesList(App.getInstance().getUserAnimeList());

        if (delegate != null){
            delegate.malDataRead();
        }
    }
}
