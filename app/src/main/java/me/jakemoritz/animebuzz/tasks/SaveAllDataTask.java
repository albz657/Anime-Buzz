package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import java.util.concurrent.CopyOnWriteArrayList;

import me.jakemoritz.animebuzz.data.AlarmsDataHelper;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SaveAllDataTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getInstance());

        App.getInstance().getDatabase().beginTransaction();

        CopyOnWriteArrayList<Season> allAnimeSeasonsSafe = new CopyOnWriteArrayList<Season>(App.getInstance().getAllAnimeSeasons());
        SeriesList allAnime = new SeriesList();
        for (Season season : allAnimeSeasonsSafe){
            allAnime.addAll(season.getSeasonSeries());
        }

        databaseHelper.saveSeriesList(allAnime);

        databaseHelper.saveSeriesList(App.getInstance().getUserAnimeList());

        for (SeasonMetadata metadata : App.getInstance().getSeasonsList()){
            databaseHelper.saveSeasonMetadata(metadata);
        }

        for (AlarmHolder alarmHolder : App.getInstance().getAlarms()){
            AlarmsDataHelper.getInstance().saveAlarm(alarmHolder);
        }

        App.getInstance().getDatabase().setTransactionSuccessful();
        App.getInstance().getDatabase().endTransaction();

        return null;
    }
}
