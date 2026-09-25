package ru.spavlochev.auth.controller;

import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.spavlochev.auth.openapi.dto.ErrorResponseDto;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityExistsException(EntityExistsException e) {
        log.warn("Неудачная попытка регистрации", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto()
                        .code(HttpStatus.CONFLICT.value())
                        .message(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException e) {
        log.error("Ошибка аутентификации", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto()
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .message("Invalid credentials. " + e.getClass().getSimpleName() + " " + e.getMessage()));
    }
}
