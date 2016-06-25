package me.jakemoritz.animebuzz.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatHelper {


    public Long getLocalTimeFromStringDate(String stringDate) {
        Date dateInTokyo;
        try {
            SimpleDateFormat tokyoDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            tokyoDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            dateInTokyo = tokyoDateFormat.parse(stringDate);

            SimpleDateFormat localDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String localDateString = localDateFormat.format(dateInTokyo);
            Date dateLocal = localDateFormat.parse(localDateString);
            return dateLocal.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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

    public Calendar getCalFromSeconds(int seconds) {
        Long ms = Long.valueOf(String.valueOf(seconds) + "000");

        Date date = new Date(ms);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal;
    }
}
