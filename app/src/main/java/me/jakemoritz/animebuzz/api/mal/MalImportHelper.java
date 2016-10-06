package me.jakemoritz.animebuzz.api.mal;

import java.util.List;

import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

class MalImportHelper {

    private MalDataImportedListener malDataImportedListener;
    private SeriesFragment fragment;

    MalImportHelper(SeriesFragment seriesFragment, MalDataImportedListener malDataImportedListener) {
        this.fragment = seriesFragment;
        this.malDataImportedListener = malDataImportedListener;
    }

    void matchSeries(List<MatchHolder> matchList) {
        SeriesList matchedSeries = new SeriesList();

        for (MatchHolder matchHolder : matchList) {
            for (Series savedSeries : App.getInstance().getAiringList()) {
                if (matchHolder.getMALID() == savedSeries.getMALID() && savedSeries.getShowType().equals("TV")) {
                    savedSeries.setInUserList(true);
                    savedSeries.setEpisodesWatched(matchHolder.getEpisodesWatched());
                    matchedSeries.add(savedSeries);
                    break;
                }
            }
        }

        SeriesList removedSeries = new SeriesList();
        if (App.getInstance().getUserAnimeList().isEmpty()) {
            App.getInstance().getUserAnimeList().addAll(matchedSeries);
        } else {
            for (Series series : App.getInstance().getUserAnimeList()) {
                if (!matchedSeries.contains(series) || !series.getShowType().equals("TV")) {
                    series.setInUserList(false);
                    AlarmHelper.getInstance().removeAlarm(series);
                    removedSeries.add(series);
                }
            }

            App.getInstance().getUserAnimeList().removeAll(removedSeries);
            App.getInstance().getUserAnimeList().addAll(matchedSeries);
        }

        Series.saveInTx(removedSeries);

        for (Series series : App.getInstance().getUserAnimeList()) {
            if (series.getAirdate() > 0 || series.getSimulcast_airdate() > 0) {
                AlarmHelper.getInstance().makeAlarm(series);
            }
        }

        if (fragment.getmAdapter() != null) {
            fragment.getmAdapter().getVisibleSeries().clear();
            fragment.getmAdapter().getVisibleSeries().addAll((SeriesList) App.getInstance().getUserAnimeList().clone());
        }

        if (malDataImportedListener != null) {
            malDataImportedListener.malDataImported(true);
        }
    }
}
