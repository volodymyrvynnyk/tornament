package com.example.tournament.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionInterceptor {

    @ExceptionHandler(ServiceException.class)
    public ErrorResponse ServiceError(ServiceException serviceException) {
        return ErrorResponse.builder()
                .message(serviceException.getMessage())
                .status(500)
                .build();
    }

}
