package me.jakemoritz.animebuzz.helpers.comparators;

import java.util.Comparator;

import me.jakemoritz.animebuzz.models.Series;

public class NextEpisodeAiringTimeComparator implements Comparator<Series> {
    @Override
    public int compare(Series lhs, Series rhs) {
        return Long.valueOf(lhs.getNextEpisodeAirtime()).compareTo(Long.valueOf(rhs.getNextEpisodeAirtime()));
    }
}
