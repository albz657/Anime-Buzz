package me.jakemoritz.animebuzz.api.mal;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.Iterator;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.fragments.MyShowsFragment;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataRead;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class MalImportHelper {

    private MalDataRead delegate;
    private SeriesFragment fragment;

    public MalImportHelper(SeriesFragment seriesFragment, MalDataRead delegate) {
        this.fragment = seriesFragment;
        this.delegate = delegate;
    }

    public void matchSeries(List<MatchHolder> matchList) {
        SeriesList matchedSeries = new SeriesList();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        String latestSeasonName = sharedPreferences.getString(App.getInstance().getString(R.string.shared_prefs_latest_season), "");
        Season latestSeason = null;

        if (!latestSeasonName.isEmpty()) {
            for (Season season : App.getInstance().getAllAnimeSeasons()) {
                if (season.getSeasonMetadata().getName().equals(latestSeasonName)) {
                    latestSeason = season;
                    break;
                }
            }
        }

        if (latestSeason != null) {
            MatchHolder match = null;
            for (Iterator matchedIterator = matchList.iterator(); matchedIterator.hasNext(); ) {
                match = (MatchHolder) matchedIterator.next();
                for (Series savedSeries : latestSeason.getSeasonSeries()) {
                    if (match.getMALID() == savedSeries.getMALID()) {
                        savedSeries.setInUserList(true);
                        savedSeries.setEpisodesWatched(match.getEpisodesWatched());
                        matchedSeries.add(savedSeries);
                        matchedIterator.remove();
                        break;
                    }
                }
            }

        }

/*        DatabaseHelper dbHelper = DatabaseHelper.getInstance(App.getInstance());

        for (MatchHolder match : matchList) {

            Series tempSeries = dbHelper.getSeries(match.getMALID());

            if (tempSeries != null) {
                tempSeries.setInUserList(true);
                tempSeries.setEpisodesWatched(match.getEpisodesWatched());
                matchedSeries.add(tempSeries);

//                App.getInstance().getCircleBitmap(tempSeries);
            }
        }*/

        App.getInstance().getUserAnimeList().addAll(matchedSeries);
        for (Series series : App.getInstance().getUserAnimeList()) {
            if (series.getAirdate() > 0 && series.getSimulcast_airdate() > 0) {
                App.getInstance().makeAlarm(series);
            }
        }

        if (fragment.getmAdapter() != null) {
            fragment.getmAdapter().setVisibleSeries((SeriesList) App.getInstance().getUserAnimeList().clone());
        }


        if (fragment instanceof MyShowsFragment) {
            ((MyShowsFragment) fragment).loadUserSortingPreference();
        }

/*        if (dbHelper != null) {
            dbHelper.saveSeriesList(App.getInstance().getUserAnimeList());
        }*/

        if (delegate != null) {
            delegate.malDataRead();
        }
    }
}
