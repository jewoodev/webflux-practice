package com.heri2go.chat.handler;

import com.heri2go.chat.handler.response.ErrorResponse;
import com.heri2go.chat.web.exception.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatedUsernameException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicatedUsernameException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(JsonConvertException.class)
    public ResponseEntity<ErrorResponse> handle(JsonConvertException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ResourceNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse(NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handle(UnauthorizedException e) {
        return ResponseEntity.status(UNAUTHORIZED).body(new ErrorResponse(UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(UserNotFoundException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handle(DataAccessException e) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handle(BadCredentialsException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception e) {
        return ResponseEntity.internalServerError().body(new ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
