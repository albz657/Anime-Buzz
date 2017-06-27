package me.jakemoritz.animebuzz.utils.comparators;

import java.util.Comparator;

import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.models.Series;

public class SeriesNameComparator implements Comparator<Series> {
    @Override
    public int compare(Series series, Series t1) {
        String firstName;
        if (SharedPrefsUtils.getInstance().prefersEnglish() && !series.getEnglishTitle().isEmpty()){
            firstName = series.getEnglishTitle();
        } else {
            firstName = series.getName();
        }

        String secondName;
        if (SharedPrefsUtils.getInstance().prefersEnglish() && !t1.getEnglishTitle().isEmpty()){
            secondName = t1.getEnglishTitle();
        } else {
            secondName = t1.getName();
        }
        return firstName.compareTo(secondName);
    }
}
