package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import me.jakemoritz.animebuzz.data.AnimeDataHelper;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SaveNewSeasonTask extends AsyncTask<SeriesList, Void, Void> {

    @Override
    protected Void doInBackground(SeriesList... seriesLists) {
        AnimeDataHelper.getInstance().saveSeriesList(seriesLists[0]);

        return null;
    }
}
