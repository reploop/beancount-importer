package org.reploop.beancount;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class LocalDateTimeUtils {
    private LocalDateTimeUtils() {
    }

    /**
     * Quietly
     */
    public static LocalDateTime parseQuietly(String s, DateTimeFormatter f) {
        try {
            return LocalDateTime.parse(s, f);
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
