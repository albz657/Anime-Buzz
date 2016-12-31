package me.jakemoritz.animebuzz.api.mal;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.models.Alarm;
import me.jakemoritz.animebuzz.models.Series;

class MalImportHelper {

    private MalDataImportedListener malDataImportedListener;

    MalImportHelper(MalDataImportedListener malDataImportedListener) {
        this.malDataImportedListener = malDataImportedListener;
    }

    void updateEpisodeCounts(final List<MatchHolder> matchList){
        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Series> userList = realm.where(Series.class).equalTo("isInUserList", true).findAll();
                for (MatchHolder matchHolder : matchList){
                    Series matchedSeries = realm.where(Series.class).equalTo("MALID", matchHolder.getMALID()).findFirst();
                    if (matchedSeries != null && userList.contains(matchedSeries)){
                        matchedSeries.setEpisodesWatched(matchHolder.getEpisodesWatched());
                    }
                }
            }
        });

        if (malDataImportedListener != null){
            malDataImportedListener.malDataImported(true);
        }
    }

    void matchSeries(final List<MatchHolder> matchList) {
        final RealmList<Series> matchedSeries = new RealmList<>();

        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (final MatchHolder matchHolder : matchList) {
                    final Series series = App.getInstance().getRealm().where(Series.class).equalTo("MALID", matchHolder.getMALID()).findFirst();
                    if (series != null){
                        series.setInUserList(true);
                        series.setEpisodesWatched(matchHolder.getEpisodesWatched());
                        matchedSeries.add(series);
                    }
                }
            }
        });


        App.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (final Series series : App.getInstance().getRealm().where(Series.class).equalTo("isInUserList", true).findAll()) {
                    if (!matchedSeries.contains(series) || (!series.getShowType().equals("TV") && !series.getShowType().isEmpty())) {
                        series.setInUserList(false);
                        RealmResults<Alarm> alarms = App.getInstance().getRealm().where(Alarm.class).equalTo("MALID", series.getMALID()).findAll();
                        alarms.deleteAllFromRealm();
                    }
                }
            }
        });

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
