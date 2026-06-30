package com.nexora.nexora_web_service.shared.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized error handling: every uncaught exception becomes a uniform JSON
 * body instead of a raw stack trace. Controller-level try/catch (e.g. AuthController)
 * still wins for the cases it handles explicitly.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean validation failures on @Valid request bodies -> 400 with per-field messages. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.putIfAbsent(err.getField(), err.getDefaultMessage()));
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /** Domain validation (invalid value objects, bad arguments) -> 400. */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(baseBody(HttpStatus.BAD_REQUEST, safeMessage(ex)));
    }

    /** @PreAuthorize / method-security denials -> 403 (no detail leaked). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(baseBody(HttpStatus.FORBIDDEN, "Access denied"));
    }

    /** Controllers that throw ResponseStatusException keep their intended status. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status).body(baseBody(status, ex.getReason()));
    }

    /** Anything else -> 500 with a generic message (never a stack trace). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseBody(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message == null ? status.getReasonPhrase() : message);
        return body;
    }

    private String safeMessage(RuntimeException ex) {
        return (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Bad request"
                : ex.getMessage();
    }
}
