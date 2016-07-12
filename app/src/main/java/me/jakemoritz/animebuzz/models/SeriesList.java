package me.jakemoritz.animebuzz.models;

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
