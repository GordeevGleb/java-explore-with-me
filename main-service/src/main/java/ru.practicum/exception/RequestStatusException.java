package ru.practicum.exception;

public class RequestStatusException extends RuntimeException {
    public RequestStatusException(String message) {
        super(message);
    }
}
