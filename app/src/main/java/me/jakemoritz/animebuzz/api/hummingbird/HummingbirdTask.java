package me.jakemoritz.animebuzz.api.hummingbird;

import android.os.AsyncTask;

import me.jakemoritz.animebuzz.fragments.SeriesFragment;
import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;


public class HummingbirdTask extends AsyncTask<SeriesList, Void, Void> {

    private HummingbirdApiClient hummingbirdApiClient;

    public HummingbirdTask(SeriesFragment callback) {
        this.hummingbirdApiClient = new HummingbirdApiClient(callback);
    }

    @Override
    protected Void doInBackground(SeriesList... params) {
        this.hummingbirdApiClient.setSeriesList(params[0]);

        for (Series series : params[0]) {
            this.hummingbirdApiClient.getSeriesData(series);
        }
        return null;
    }
}
