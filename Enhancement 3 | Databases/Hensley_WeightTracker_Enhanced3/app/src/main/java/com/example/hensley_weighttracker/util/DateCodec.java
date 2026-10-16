package com.example.hensley_weighttracker.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class DateCodec {
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter
            .ofPattern("MM/dd/uuuu", Locale.US).withResolverStyle(ResolverStyle.STRICT);

    private DateCodec() { }

    public static String toStored(String value) {
        if (value == null) throw new IllegalArgumentException("A date is required.");
        LocalDate date = value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")
                ? LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.parse(value, DISPLAY);
        if (date.getYear() < 1 || date.getYear() > 9999) {
            throw new IllegalArgumentException("Date is outside the supported range.");
        }
        return date.toString();
    }

    public static String toDisplay(String stored) {
        return LocalDate.parse(toStored(stored)).format(DISPLAY);
    }

    public static String today() {
        return LocalDate.now().toString();
    }
}
