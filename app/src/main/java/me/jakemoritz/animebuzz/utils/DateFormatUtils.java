package me.jakemoritz.animebuzz.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatUtils {

    private static DateFormatUtils dateFormatUtils;

    public synchronized static DateFormatUtils getInstance() {
        if (dateFormatUtils == null) {
            dateFormatUtils = new DateFormatUtils();
        }
        return dateFormatUtils;
    }

    // Creates a Calendar object from Kitsu date string
    public Calendar getCalFromHB(String dateString) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date;
        try {
            date = originalFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Formats Calendar date to string
    public String getAiringDateFormatted(Calendar calendar, boolean includeYear) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());

        String formattedDate = dateFormat.format(calendar.getTime()) + getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH));
        if (includeYear) {
            SimpleDateFormat yearFormat = new SimpleDateFormat(", yyyy", Locale.getDefault());
            formattedDate += yearFormat.format(calendar.getTime());
        }

        return formattedDate;
    }

    String getDayOfMonthSuffix(final int n) {
        String[] suffixes =
                //    0     1     2     3     4     5     6     7     8     9
                {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    10    11    12    13    14    15    16    17    18    19
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        //    20    21    22    23    24    25    26    27    28    29
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    30    31
                        "th", "st"};


        return suffixes[n];
    }

    Calendar getCalFromSeconds(int seconds) {
        Long ms = Long.valueOf(String.valueOf(seconds) + "000");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ms);

        return cal;
    }
}
