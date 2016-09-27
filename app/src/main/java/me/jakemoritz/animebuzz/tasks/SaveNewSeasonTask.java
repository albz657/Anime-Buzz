package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SaveNewSeasonTask extends AsyncTask<SeriesList, Void, Void> {

    @Override
    protected Void doInBackground(SeriesList... seriesLists) {
        Series.saveInTx(seriesLists[0]);

        return null;
    }
}
