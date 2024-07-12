package ru.practicum.util;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.DateTimeFormatException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class StatsDateTimeFormatter {
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    public LocalDateTime format(String time) {
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(PATTERN));
        } catch (DateTimeParseException e) {
            throw new DateTimeFormatException("Time format is incorrect.");
        }
    }
}
