package me.jakemoritz.animebuzz.api.mal;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.api.ann.models.ImageRequestHolder;
import me.jakemoritz.animebuzz.api.mal.models.MatchHolder;
import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.helpers.AlarmHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.interfaces.mal.MalDataImportedListener;
import me.jakemoritz.animebuzz.models.Season;
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

        String latestSeasonName = SharedPrefsHelper.getInstance().getLatestSeasonName();
        Season latestSeason = null;

        if (!latestSeasonName.isEmpty()) {
            for (Season season : App.getInstance().getAllAnimeSeasons()) {
                if (season.getSeasonMetadata().getName().equals(latestSeasonName)) {
                    latestSeason = season;
                    break;
                }
            }
        }

        List<ImageRequestHolder> imageRequests = new ArrayList<>();

        if (latestSeason != null) {
            for (MatchHolder matchHolder : matchList) {
                for (Series savedSeries : latestSeason.getSeasonSeries()) {
                    if (matchHolder.getMALID() == savedSeries.getMALID()) {
                        if (savedSeries.getANNID() == -1  && matchHolder.getImageURL() != null){
                                imageRequests.add(new ImageRequestHolder(matchHolder.getImageURL(), String.valueOf(savedSeries.getMALID()), "MAL"));
                        }

                        savedSeries.setInUserList(true);
                        savedSeries.setEpisodesWatched(matchHolder.getEpisodesWatched());
                        matchedSeries.add(savedSeries);
                        break;
                    }
                }
            }
        }

        if (!imageRequests.isEmpty()){
//            GetImageTask task = new GetImageTask(fragment);
//            task.execute(imageRequests);
        }

        SeriesList remainingSeries = new SeriesList();
        SeriesList removedSeries = new SeriesList();
        if (App.getInstance().getUserAnimeList().isEmpty()){
            App.getInstance().getUserAnimeList().addAll(matchedSeries);
        } else {
            for (Series series : App.getInstance().getUserAnimeList()){
                if (!matchedSeries.contains(series)){
                    series.setInUserList(false);
                    AlarmHelper.getInstance().removeAlarm(series);
                    removedSeries.add(series);
                }
            }

            App.getInstance().getUserAnimeList().removeAll(removedSeries);
            App.getInstance().getUserAnimeList().addAll(matchedSeries);

//            App.getInstance().getUserAnimeList().clear();
//            App.getInstance().getUserAnimeList().addAll(remainingSeries);
        }

        Series.saveInTx(removedSeries);

        for (Series series : App.getInstance().getUserAnimeList()) {
            if (series.getAirdate() > 0 && series.getSimulcast_airdate() > 0) {
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
