package za.co.flash.sensitivewords.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(SensitiveWordException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            SensitiveWordException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "message", exception.getMessage()
                ));
    }


    @ExceptionHandler(SensitiveWordException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExists(
            SensitiveWordException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.CONFLICT.value(),
                        "error", HttpStatus.CONFLICT.getReasonPhrase(),
                        "message", exception.getMessage()
                ));
    }


    @ExceptionHandler(SensitiveWordException.class)
    public ResponseEntity<Map<String, Object>> handleSensitiveWordException(SensitiveWordException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "message", exception.getMessage()
                ));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "message", message
                ));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception exception) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "message", "An unexpected error occurred"
                ));
    }
}