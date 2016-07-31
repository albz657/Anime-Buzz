package me.jakemoritz.animebuzz.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class SeriesList extends ArrayList<Series> {

    public SeriesList(Collection<? extends Series> c) {
        addAll(c);
    }

    public SeriesList() {
        super();
    }

    @Override
    public boolean add(Series series) {
        if (series.getName().equals("Momokuri")){
            Log.d("TAG", "s");
        }
        if (contains(series)) {
            int index = indexOf(series);
            Series currentEntry = get(index);

            currentEntry.setAirdate(series.getAirdate());
            currentEntry.setSimulcast(series.getSimulcast());
            currentEntry.setAirdate(series.getAirdate());
            currentEntry.setSimulcast_airdate(series.getSimulcast_airdate());
            currentEntry.setANNID(series.getANNID());
            currentEntry.setSimulcast_delay(series.getSimulcast_delay());
            currentEntry.setSeason(series.getSeason());
            currentEntry.setEpisodesWatched(series.getEpisodesWatched());

            return true;
        } else {
            return super.add(series);
        }
    }

    @Override
    public boolean addAll(Collection<? extends Series> c) {
        for (Series series : c) {
            add(series);
        }
        return true;
    }
}
