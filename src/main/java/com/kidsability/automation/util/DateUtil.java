package com.kidsability.automation.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtil {
    public static LocalDate getToday() {
        ZoneId estZoneId = ZoneId.of("America/New_York");
        ZonedDateTime estDateTime = ZonedDateTime.now(estZoneId);
        return estDateTime.toLocalDate();
    }

    public static String getMDY(LocalDate localDate) {
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        int year = localDate.getYear();
        return month + "/" + day + "/" + String.valueOf(year).substring(2);
    }
}
