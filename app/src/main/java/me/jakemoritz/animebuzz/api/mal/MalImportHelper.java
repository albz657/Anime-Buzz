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
        for (final MatchHolder matchHolder : matchList) {
            final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", matchHolder.getMALID()).findFirst();
            App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    series.setInUserList(true);
                    series.setEpisodesWatched(matchHolder.getEpisodesWatched());
                }
            });

            matchedSeries.add(series);
        }

        for (final Series series : App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll()) {
            if (!matchedSeries.contains(series) || !series.getShowType().equals("TV")) {
                App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        series.setInUserList(false);

                    }
                });

                AlarmHelper.getInstance().removeAlarm(series);
            }
        }


        for (Series series : App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll()) {
            if (series.getNextEpisodeAirtime() > 0 || series.getNextEpisodeSimulcastTime() > 0) {
                AlarmHelper.getInstance().makeAlarm(series);
            }
        }

        if (malDataImportedListener != null) {
            malDataImportedListener.malDataImported(true);
        }
    }
}
