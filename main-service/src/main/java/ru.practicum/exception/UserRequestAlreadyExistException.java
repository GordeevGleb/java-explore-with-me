package ru.practicum.exception;

public class UserRequestAlreadyExistException extends RuntimeException {
    public UserRequestAlreadyExistException(String message) {
        super(message);
    }
}
