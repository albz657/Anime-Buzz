package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SaveNewSeasonTask extends AsyncTask<SeriesList, Void, Void> {

    @Override
    protected Void doInBackground(SeriesList... seriesLists) {
        DatabaseHelper.getInstance(App.getInstance()).saveSeriesList(seriesLists[0]);

        return null;
    }
}
