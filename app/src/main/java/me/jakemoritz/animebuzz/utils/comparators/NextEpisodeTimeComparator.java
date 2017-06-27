package me.jakemoritz.animebuzz.utils.comparators;

import java.util.Comparator;

import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.models.Series;

public class NextEpisodeTimeComparator implements Comparator<Series> {
    @Override
    public int compare(Series lhs, Series rhs) {
        Long firstTime;
        if (SharedPrefsUtils.getInstance().prefersSimulcast() && lhs.getNextEpisodeSimulcastTime() > 0){
            firstTime = lhs.getNextEpisodeSimulcastTime();
        } else {
            firstTime = lhs.getNextEpisodeAirtime();
        }

        Long secondTime;
        if (SharedPrefsUtils.getInstance().prefersSimulcast() && rhs.getNextEpisodeSimulcastTime() > 0){
            secondTime = rhs.getNextEpisodeSimulcastTime();
        } else {
            secondTime = rhs.getNextEpisodeAirtime();
        }
        return firstTime.compareTo(secondTime);
    }
}
