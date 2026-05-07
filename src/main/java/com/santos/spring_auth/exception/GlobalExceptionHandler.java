package com.santos.spring_auth.exception;

import com.santos.spring_auth.dto.error.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return this.build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorDTO> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return this.build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return this.build(HttpStatus.FORBIDDEN, "Access denied", request, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorDTO> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return this.build(HttpStatus.UNAUTHORIZED, "Invalid credentials", request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorDTO.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        return this.build(HttpStatus.BAD_REQUEST, "Validation failed", request, violations);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDTO> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return this.build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return this.build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request, null);
    }

    private ApiErrorDTO.FieldViolation toViolation(FieldError fieldError) {
        return new ApiErrorDTO.FieldViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorDTO> build(HttpStatus status,
                                              String message,
                                              HttpServletRequest request,
                                              List<ApiErrorDTO.FieldViolation> violations) {
        ApiErrorDTO body = new ApiErrorDTO(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                violations
        );
        return ResponseEntity.status(status).body(body);
    }
}
