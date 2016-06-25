package me.jakemoritz.animebuzz.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class SeasonComparator implements Comparator<Season> {

    @Override
    public int compare(Season lhs, Season rhs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
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
