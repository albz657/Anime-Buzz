package me.jakemoritz.animebuzz.api.mal;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.Series;

class MalImportHelper {

    private MalDataImportedListener malDataImportedListener;
    private SeriesFragment fragment;

    MalImportHelper(SeriesFragment seriesFragment, MalDataImportedListener malDataImportedListener) {
        this.fragment = seriesFragment;
        this.malDataImportedListener = malDataImportedListener;
    }

    void matchSeries(final List<MatchHolder> matchList) {
        final RealmList<Series> matchedSeries = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (final MatchHolder matchHolder : matchList) {
                    final Series series = realm.where(Series.class).equalTo("MALID", matchHolder.getMALID()).findFirst();
                    if (series != null){
                        series.setInUserList(true);
                        series.setEpisodesWatched(matchHolder.getEpisodesWatched());
                        matchedSeries.add(series);
                    }
                }
            }
        });


        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (final Series series : realm.where(Series.class).equalTo("isInUserList", true).findAll()) {
                    if (!matchedSeries.contains(series) || (!series.getShowType().equals("TV") && !series.getShowType().isEmpty())) {
                        series.setInUserList(false);
                        RealmResults<Alarm> alarms = realm.where(Alarm.class).equalTo("MALID", series.getMALID()).findAll();
                        alarms.deleteAllFromRealm();
                    }
                }
            }
        });

        for (Series series : realm.where(Series.class).equalTo("isInUserList", true).findAll()) {
            if (series.getNextEpisodeAirtime() > 0 || series.getNextEpisodeSimulcastTime() > 0) {
                AlarmHelper.getInstance().makeAlarm(series);
            }
        }

        realm.close();

        if (malDataImportedListener != null) {
            malDataImportedListener.malDataImported(true);
        }
    }
}
