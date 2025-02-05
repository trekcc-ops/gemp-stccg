package com.gempukku.stccg;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.text.ParseException;

public class DateUtils {
    public static int getCurrentDateAsInt() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
        return Integer.parseInt(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    public static int offsetDate(int start, int dayOffset) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Calendar c = Calendar.getInstance();
            c.setTime(format.parse(String.valueOf(start)));
            c.add(Calendar.DATE, dayOffset);
            return(Integer.parseInt(format.format(c.getTime())));
        } catch (ParseException exp) {
            throw new RuntimeException("Can't parse date", exp);
        }
    }

    public static int getMondayBeforeOrOn(ZonedDateTime date) {
        ZonedDateTime lastMonday = date.minusDays(date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        return Integer.parseInt(lastMonday.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

}