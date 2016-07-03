package me.jakemoritz.animebuzz.models;

import java.util.Comparator;

public class NextEpisodeAiringTimeComparator implements Comparator<Series> {
    @Override
    public int compare(Series lhs, Series rhs) {
        return Long.valueOf(lhs.getNextEpisodeSimulcastTime()).compareTo(Long.valueOf(rhs.getNextEpisodeSimulcastTime()));
    }
}
