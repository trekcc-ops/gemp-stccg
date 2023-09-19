package com.gempukku.lotro;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static int getCurrentDateAsInt() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
        return Integer.parseInt(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    public static String getCurrentDateAsString() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static int offsetDate(int start, int dayOffset) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        ZonedDateTime startDate = ZonedDateTime.parse(String.valueOf(start), formatter);
        ZonedDateTime endDate = startDate.plusDays(dayOffset);
        return Integer.parseInt(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    public static int getMondayBeforeOrOn(ZonedDateTime date) {
        ZonedDateTime lastMonday = date.minusDays(date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        return Integer.parseInt(lastMonday.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

}
