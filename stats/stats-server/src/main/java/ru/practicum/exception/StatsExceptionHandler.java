package ru.practicum.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class StatsExceptionHandler {

    public Map<String, String> handleDateTimeFormatException(final DateTimeFormatException e) {
        return Map.of("ERROR: ", e.getMessage());
    }
}
