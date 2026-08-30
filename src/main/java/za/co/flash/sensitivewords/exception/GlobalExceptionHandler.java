package za.co.flash.sensitivewords.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SensitiveWordException.class)
    public ResponseEntity<Map<String, Object>> handleSensitiveWordException(SensitiveWordException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "message", exception.getMessage()
                ));
    }

    // TODO: Add specific exception handlers for validation, authentication,
    //  authorization, resource not found and unexpected server errors.
}