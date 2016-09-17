package me.jakemoritz.animebuzz.models;

import android.databinding.ObservableArrayList;
import android.util.Log;

import java.util.Collection;

public class SeriesList extends ObservableArrayList<Series> {

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
            currentEntry.setEpisodesWatched(series.getEpisodesWatched());

            if (series.getNextEpisodeAirtime() != 0){
                currentEntry.setNextEpisodeAirtime(series.getNextEpisodeAirtime());
            }

            if (series.getNextEpisodeSimulcastTime() != 0){
                currentEntry.setNextEpisodeSimulcastTime(series.getNextEpisodeSimulcastTime());
            }

            if (!series.getNextEpisodeAirtimeFormatted().isEmpty()){
                currentEntry.setNextEpisodeAirtimeFormatted(series.getNextEpisodeAirtimeFormatted());
            }
            if (!series.getNextEpisodeSimulcastTimeFormatted().isEmpty()){
                currentEntry.setNextEpisodeSimulcastTimeFormatted(series.getNextEpisodeSimulcastTimeFormatted());
            }
            if (!series.getNextEpisodeAirtimeFormatted24().isEmpty()){
                currentEntry.setNextEpisodeAirtimeFormatted24(series.getNextEpisodeAirtimeFormatted24());
            }
            if (!series.getNextEpisodeSimulcastTimeFormatted24().isEmpty()){
                currentEntry.setNextEpisodeSimulcastTimeFormatted24(series.getNextEpisodeSimulcastTimeFormatted24());
            }

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
