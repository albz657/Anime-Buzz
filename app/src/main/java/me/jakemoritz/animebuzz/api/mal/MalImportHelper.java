package me.jakemoritz.animebuzz.api.mal;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.models.Series;

class MalImportHelper {

    private MalDataImportedListener malDataImportedListener;
    private SeriesFragment fragment;

    MalImportHelper(SeriesFragment seriesFragment, MalDataImportedListener malDataImportedListener) {
        this.fragment = seriesFragment;
        this.malDataImportedListener = malDataImportedListener;
    }

    void matchSeries(List<MatchHolder> matchList) {
        RealmList<Series> matchedSeries = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
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

        RealmList<Series> removedSeries = new RealmList<>();
        if (App.getInstance().getUserList().isEmpty()) {
            App.getInstance().getUserList().addAll(matchedSeries);
        } else {
            for (Series series : App.getInstance().getUserList()) {
                if (!matchedSeries.contains(series) || !series.getShowType().equals("TV")) {
                    series.setInUserList(false);
                    AlarmHelper.getInstance().removeAlarm(series);
                    removedSeries.add(series);
                }
            }

            App.getInstance().getUserList().removeAll(removedSeries);
            App.getInstance().getUserList().addAll(matchedSeries);
        }

        realm.commitTransaction();

        for (Series series : App.getInstance().getUserList()) {
            if (series.getNextEpisodeAirtime() > 0 || series.getNextEpisodeSimulcastTime() > 0) {
                AlarmHelper.getInstance().makeAlarm(series);
            }
        }

        if (fragment.getmAdapter() != null) {
            fragment.getmAdapter().getData().clear();
            fragment.getmAdapter().getData().addAll(App.getInstance().getUserList());
        }

        if (malDataImportedListener != null) {
            malDataImportedListener.malDataImported(true);
        }
    }
}
