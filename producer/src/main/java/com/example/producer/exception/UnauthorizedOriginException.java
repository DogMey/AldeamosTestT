package com.example.producer.exception;

public class UnauthorizedOriginException extends RuntimeException {
    public UnauthorizedOriginException(String message) {
        super(message);
    }
}
