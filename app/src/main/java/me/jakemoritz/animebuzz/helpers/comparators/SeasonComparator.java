package me.jakemoritz.animebuzz.helpers.comparators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import me.jakemoritz.animebuzz.models.Season;

public class SeasonComparator implements Comparator<Season> {

    @Override
    public int compare(Season lhs, Season rhs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        try {
            Date leftAsDate = dateFormat.parse(lhs.getStartTimestamp());
            Date rightAsDate = dateFormat.parse(rhs.getStartTimestamp());

            return leftAsDate.compareTo(rightAsDate);
        } catch (ParseException e){
            e.printStackTrace();
        }
        return 0;
    }
}
