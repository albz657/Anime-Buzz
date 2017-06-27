package me.jakemoritz.animebuzz.utils.comparators;

import java.util.Comparator;

import me.jakemoritz.animebuzz.models.Series;

public class NextEpisodeSimulcastTimeComparator implements Comparator<Series> {
    @Override
    public int compare(Series lhs, Series rhs) {
        return Long.valueOf(lhs.getNextEpisodeSimulcastTime()).compareTo(Long.valueOf(rhs.getNextEpisodeSimulcastTime()));
    }
}
