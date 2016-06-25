package me.jakemoritz.animebuzz.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatHelper {

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
