package me.jakemoritz.animebuzz.helpers.comparators;

import java.util.Comparator;

import me.jakemoritz.animebuzz.models.Series;

public class SeriesNameComparator implements Comparator<Series> {
    @Override
    public int compare(Series series, Series t1) {
        return series.getName().compareTo(t1.getName());
    }
}
