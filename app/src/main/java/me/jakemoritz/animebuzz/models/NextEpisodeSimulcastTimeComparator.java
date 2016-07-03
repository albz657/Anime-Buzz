package me.jakemoritz.animebuzz.models;

import java.util.Comparator;

public class NextEpisodeSimulcastTimeComparator implements Comparator<Series> {
    @Override
    public int compare(Series lhs, Series rhs) {
        return Long.valueOf(lhs.getNextEpisodeAirtime()).compareTo(Long.valueOf(rhs.getNextEpisodeAirtime()));
    }
}
