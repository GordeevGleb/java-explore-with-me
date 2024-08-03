package ru.practicum.exception;

public class IntegrityConflictException extends RuntimeException {
    public IntegrityConflictException(String message) {
        super(message);
    }
}
