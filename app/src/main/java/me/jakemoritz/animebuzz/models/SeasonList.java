package me.jakemoritz.animebuzz.models;

import java.util.ArrayList;
import java.util.Collection;

public class SeasonList extends ArrayList<Season> {

    @Override
    public boolean add(Season season) {
        if (contains(season)){
            int index = indexOf(season);
            Season currentEntry = get(index);

            currentEntry.getSeasonSeries().addAll(season.getSeasonSeries());
            return true;
        } else {
            return super.add(season);
        }
    }

    @Override
    public boolean addAll(Collection<? extends Season> c) {
        return super.addAll(c);
    }

    public SeasonList(Collection<? extends Season> c) {
        addAll(c);
    }

    public SeasonList() {
        super();
    }
}
