package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.AlarmHolder;
import me.jakemoritz.animebuzz.models.Season;
import me.jakemoritz.animebuzz.models.SeasonMetadata;

public class SaveAllDataTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getInstance());

        CopyOnWriteArrayList<Season> safeAllAnimeList = new CopyOnWriteArrayList<Season>(App.getInstance().getAllAnimeSeasons());
        for (Iterator iterator = safeAllAnimeList.iterator(); iterator.hasNext();){
            databaseHelper.saveSeriesList(((Season) iterator.next()).getSeasonSeries());
        }

        databaseHelper.saveSeriesList(App.getInstance().getUserAnimeList());

        for (SeasonMetadata metadata : App.getInstance().getSeasonsList()){
            databaseHelper.saveSeasonMetadata(metadata);
        }

        for (AlarmHolder alarmHolder : App.getInstance().getAlarms()){
            databaseHelper.saveAlarm(alarmHolder);
        }

        return null;
    }
}
