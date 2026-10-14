package com.example.bike_auction_platform.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            Map<String, String> validationErrors
    ) {}

    private ResponseEntity<ApiError> createError(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors
    ) {
        return ResponseEntity.status(status).body(
                new ApiError(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        validationErrors
                )
        );
    }

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            AuctionNotFoundException exception
    ) {
        return createError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                Map.of()
        );
    }

    @ExceptionHandler(InvalidBidException.class)
    public ResponseEntity<ApiError> handleInvalidBid(
            InvalidBidException exception
    ) {
        return createError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException exception
    ) {
        return createError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                Map.of()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleInvalidState(
            IllegalStateException exception
    ) {
        return createError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new TreeMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value"
                            : fieldError.getDefaultMessage()
            );
        }

        return createError(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody() {
        return createError(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON or invalid field format",
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataConflict() {
        return createError(
                HttpStatus.CONFLICT,
                "The request conflicts with existing data",
                Map.of()
        );
    }
}