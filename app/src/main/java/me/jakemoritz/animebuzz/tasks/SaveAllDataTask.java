package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import java.util.concurrent.CopyOnWriteArrayList;

import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.Series;

public class SaveAllDataTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = SaveAllDataTask.class.getSimpleName();

    @Override
    protected Void doInBackground(Void... voids) {

        CopyOnWriteArrayList<Season> allAnimeSeasonsSafe = new CopyOnWriteArrayList<Season>(App.getInstance().getAllAnimeSeasons());
        for (Season season : allAnimeSeasonsSafe) {
            Series.saveInTx(season.getSeasonSeries());
        }

        Series.saveInTx(App.getInstance().getUserAnimeList());

        SeasonMetadata.saveInTx(App.getInstance().getSeasonsList());

        AlarmHolder.saveInTx(App.getInstance().getAlarms());

        return null;
    }
}
