package ru.practicum.exception;

public class ConcurrentNameException extends RuntimeException {
    public ConcurrentNameException(String message) {
        super(message);
    }
}
