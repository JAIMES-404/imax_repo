package com.imax.app.utils;

public class UnauthorizedException extends Exception {

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}