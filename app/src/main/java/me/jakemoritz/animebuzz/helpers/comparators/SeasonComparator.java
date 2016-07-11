package me.jakemoritz.animebuzz.helpers.comparators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import me.jakemoritz.animebuzz.models.Season;

public class SeasonComparator implements Comparator<Season> {
    @Override
    public int compare(Season lhs, Season rhs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            Date leftAsDate = dateFormat.parse(lhs.getSeasonMetadata().getStart_timestamp());
            Date rightAsDate = dateFormat.parse(rhs.getSeasonMetadata().getStart_timestamp());

            return leftAsDate.compareTo(rightAsDate);
        } catch (ParseException e){
            e.printStackTrace();
        }
        return 0;
    }
}
