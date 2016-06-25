package me.jakemoritz.animebuzz.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatHelper {



    public Long getLocalTimeFromStringDate(String stringDate){
        Date dateInTokyo;
        try{
            SimpleDateFormat tokyoDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            tokyoDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            dateInTokyo = tokyoDateFormat.parse(stringDate);

            SimpleDateFormat localDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String localDateString = localDateFormat.format(dateInTokyo);
            Date dateLocal = localDateFormat.parse(localDateString);
            return dateLocal.getTime();
        } catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    public String getLocalFormattedDate(String stringDate){
        Date date;
        try{
            SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            date = originalFormat.parse(stringDate);

            DateFormat localFormat = SimpleDateFormat.getDateInstance();

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String formattedDate = localFormat.format(date);
            return formattedDate;
        } catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

}
