package com.github.youlfey.guess.the.number.game.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    public static Long getSecondsWithNow(LocalDateTime from) {
        return getSeconds(from, LocalDateTime.now());
    }

    public static Long getMilliSecondsWithNow(LocalDateTime from) {
        return getMilliSeconds(from, LocalDateTime.now());
    }

    private static Long getMilliSeconds(LocalDateTime from, LocalDateTime to) {
        Duration between = Duration.between(from, to);
        return between.toMillis();
    }

    private static Long getSeconds(LocalDateTime from, LocalDateTime to) {
        Duration duration = Duration.between(from, to);
        return duration.getSeconds();
    }

    public static Date convertFromLocalDateTime(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime localDateTimePlusMillis(LocalDateTime ldt, Long millis) {
        return millsToLocalDateTime(toEpochMilli(ldt) + millis);
    }

    public static Long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static LocalDateTime millsToLocalDateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Long minutesToMilliSeconds(Long minutes) {
        return minutes * 60 * 1000;
    }
}
