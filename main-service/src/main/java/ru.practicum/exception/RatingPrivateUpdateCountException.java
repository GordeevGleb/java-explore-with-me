package ru.practicum.exception;

public class RatingPrivateUpdateCountException extends RuntimeException {
    public RatingPrivateUpdateCountException(String message) {
        super(message);
    }
}
