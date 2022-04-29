package com.example.finance.api.common.exception.handler;

import com.example.finance.api.common.exception.BusinessException;
import com.example.finance.api.common.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String GENERIC_ERROR_MESSAGE =
            "An unexpected internal system error has occurred";

    @ExceptionHandler
    public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException e) {
        var status = HttpStatus.NOT_FOUND;
        ApiError error = buildApiError(status.value(), e.getMessage());
        return responseEntity(status, error);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleBusinessException(BusinessException e) {
        var status = HttpStatus.BAD_REQUEST;
        ApiError error = buildApiError(status.value(), e.getMessage());
        return responseEntity(status, error);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var status = HttpStatus.BAD_REQUEST;
        var errors = ex.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
        var body = buildApiError(status.value(), errors);
        return responseEntity(status, body);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleUncaught(Exception ex) {
        log.error(ex.getMessage(), ex);
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var error = buildApiError(status.value(), GENERIC_ERROR_MESSAGE);
        return responseEntity(status, error);
    }

    private ApiError buildApiError(Integer code, String message) {
        return new ApiError(code, List.of(message), Instant.now());
    }

    private ApiError buildApiError(Integer code, List<String> messages) {
        return new ApiError(code, messages, Instant.now());
    }

    private ResponseEntity<ApiError> responseEntity(HttpStatus status, ApiError body) {
        return ResponseEntity
                    .status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
    }

}