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
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmList<Series> matchedSeries = new RealmList<>();
        for (MatchHolder matchHolder : matchList) {
            Series series = realm.where(Series.class).equalTo("MALID", matchHolder.getMALID()).findFirst();
            series.setInUserList(true);
            series.setEpisodesWatched(matchHolder.getEpisodesWatched());
            matchedSeries.add(series);
        }

        for (Series series : App.getInstance().getUserList()) {
            if (!matchedSeries.contains(series) || !series.getShowType().equals("TV")) {
                series.setInUserList(false);
                AlarmHelper.getInstance().removeAlarm(series);
            }
        }

        realm.commitTransaction();

        for (Series series : App.getInstance().getUserList()) {
            if (series.getNextEpisodeAirtime() > 0 || series.getNextEpisodeSimulcastTime() > 0) {
                AlarmHelper.getInstance().makeAlarm(series);
            }
        }

        if (malDataImportedListener != null) {
            malDataImportedListener.malDataImported(true);
        }
    }
}
