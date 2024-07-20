package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.entity.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class MainServiceExceptionHandler {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleConcurrentUserNameException(final ConcurrentNameException e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

@ExceptionHandler
@ResponseStatus(HttpStatus.NOT_FOUND)
@ResponseBody
    public ApiError handleNotFoundException(final NotFoundException e) {
        return ApiError.builder()
                .status("NOT_FOUND")
                .reason("The required object was not found.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleDateTimeException(final DateTimeException e) {
        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleIntegrityConflictException(final IntegrityConflictException e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleUserRequestAlreadyExistException(final UserRequestAlreadyExistException e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleLimitException(final LimitException e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleEventStatusException(final EventStatusException e) {
        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleWrongRequestException(final WrongRequestException e) {
        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleRequestStatusException(final RequestStatusException e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleWrongInputException(final WrongInputException e) {
        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
    }
}
