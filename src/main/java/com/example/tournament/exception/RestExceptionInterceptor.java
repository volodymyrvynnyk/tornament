package com.example.tournament.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionInterceptor {

    @ExceptionHandler(ServiceException.class)
    public ErrorResponse ServiceError(ServiceException serviceException) {
        return ErrorResponse.builder()
                .message(serviceException.getMessage())
                .status(500)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse ValidationError(MethodArgumentNotValidException exception) {

        List<String> validationErrors = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return ErrorResponse.builder()
                .message("Validation errors: " + String.join(", ", validationErrors))
                .status(500)
                .build();
    }



}
