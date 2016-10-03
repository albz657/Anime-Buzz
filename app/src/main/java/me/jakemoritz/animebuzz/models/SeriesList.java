package me.jakemoritz.animebuzz.models;

import android.databinding.ObservableArrayList;

import java.util.Collection;

import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;

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

            if (currentEntry.isShifted() && currentEntry.getSeason().equals(SharedPrefsHelper.getInstance().getLatestSeasonName())){
//                return false;
            }

            currentEntry.setAirdate(series.getAirdate());
            currentEntry.setSimulcast(series.getSimulcast());
            currentEntry.setAirdate(series.getAirdate());
            currentEntry.setSimulcast_airdate(series.getSimulcast_airdate());
            currentEntry.setANNID(series.getANNID());
            currentEntry.setSimulcast_delay(series.getSimulcast_delay());
            currentEntry.setEpisodesWatched(series.getEpisodesWatched());
            currentEntry.setFinishedAiringDate(series.getFinishedAiringDate());
            currentEntry.setStartedAiringDate(series.getStartedAiringDate());
            currentEntry.setSingle(series.isSingle());
            currentEntry.setShowType(series.getShowType());

            if (currentEntry.getSeason().isEmpty()){
                currentEntry.setSeason(series.getSeason());
            }

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

            if (!series.getEnglishTitle().isEmpty() && currentEntry.getEnglishTitle().isEmpty()){
                currentEntry.setEnglishTitle(series.getEnglishTitle());
            }

            if (!series.getAiringStatus().isEmpty()){
                currentEntry.setAiringStatus(series.getAiringStatus());
            }



            return true;
        } else {
            return super.add(series);
        }
    }

    public Series getAnime(Long MALID){
        for (Series series : this){
            if (series.getMALID().equals(MALID)){
                return series;
            }
        }
        return null;
    }

    @Override
    public boolean addAll(Collection<? extends Series> c) {
        for (Series series : c) {
            add(series);
        }
        return true;
    }
}
