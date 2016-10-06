package me.jakemoritz.animebuzz.helpers.comparators;

import java.util.Comparator;

import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Series;

public class NextEpisodeTimeComparator implements Comparator<Series> {
    @Override
    public int compare(Series lhs, Series rhs) {
        Long firstTime;
        if (SharedPrefsHelper.getInstance().prefersSimulcast() && lhs.getNextEpisodeSimulcastTime() > 0){
            firstTime = lhs.getNextEpisodeSimulcastTime();
        } else {
            firstTime = lhs.getNextEpisodeAirtime();
        }

        Long secondTime;
        if (SharedPrefsHelper.getInstance().prefersSimulcast() && rhs.getNextEpisodeSimulcastTime() > 0){
            secondTime = rhs.getNextEpisodeSimulcastTime();
        } else {
            secondTime = rhs.getNextEpisodeAirtime();
        }
        return firstTime.compareTo(secondTime);
    }
}
