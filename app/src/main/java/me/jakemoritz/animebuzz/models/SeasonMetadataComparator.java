package me.jakemoritz.animebuzz.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class SeasonMetadataComparator implements Comparator<SeasonMetadata> {

    @Override
    public int compare(SeasonMetadata lhs, SeasonMetadata rhs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            Date leftAsDate = dateFormat.parse(lhs.getStart_timestamp());
            Date rightAsDate = dateFormat.parse(rhs.getStart_timestamp());

            return leftAsDate.compareTo(rightAsDate);
        } catch (ParseException e){
            e.printStackTrace();
        }
        return 0;
    }
}
