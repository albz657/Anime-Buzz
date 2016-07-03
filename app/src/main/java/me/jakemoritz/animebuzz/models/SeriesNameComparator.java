package me.jakemoritz.animebuzz.models;

import java.util.Comparator;

public class SeriesNameComparator implements Comparator<Series> {
    @Override
    public int compare(Series series, Series t1) {
        return series.getName().compareTo(t1.getName());
    }
}
