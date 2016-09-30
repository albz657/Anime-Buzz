package me.jakemoritz.animebuzz.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateFormatHelper {

    private static DateFormatHelper dateFormatHelper;

    public synchronized static DateFormatHelper getInstance(){
        if (dateFormatHelper == null){
            dateFormatHelper = new DateFormatHelper();
        }
        return dateFormatHelper;
    }

    public String getLocalFormattedDateFromStringDate(String stringDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = originalFormat.parse(stringDate);

            DateFormat localFormat = SimpleDateFormat.getDateInstance();

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return localFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDayOfMonthSuffix(final int n) {
        String[] suffixes =
                //    0     1     2     3     4     5     6     7     8     9
                { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    10    11    12    13    14    15    16    17    18    19
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        //    20    21    22    23    24    25    26    27    28    29
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    30    31
                        "th", "st" };


        return suffixes[n];
    }

    public Calendar getCalFromSeconds(int seconds) {
        Long ms = Long.valueOf(String.valueOf(seconds) + "000");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ms);

        return cal;
    }
}
