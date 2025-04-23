package com.heri2go.chat.web.exception;

public class DuplicatedUsernameException extends RuntimeException {
    public DuplicatedUsernameException(String message) {
        super(message);
    }
}
