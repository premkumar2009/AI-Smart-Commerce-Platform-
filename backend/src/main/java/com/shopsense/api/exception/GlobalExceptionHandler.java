package com.shopsense.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception, jakarta.servlet.http.HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", "));
        return response(HttpStatus.BAD_REQUEST, "Validation Error", message, request.getRequestURI());
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> status(ResponseStatusException exception, jakarta.servlet.http.HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return response(status, status.getReasonPhrase(), exception.getReason(), request.getRequestURI());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception exception, jakarta.servlet.http.HttpServletRequest request) {
        logger.error("Unexpected API error for {}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something went wrong. Please try again.", request.getRequestURI());
    }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, String error, String message, String path) { return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), error, message, path)); }
}
