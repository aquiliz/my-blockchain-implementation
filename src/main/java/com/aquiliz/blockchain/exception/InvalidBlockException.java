package com.aquiliz.blockchain.exception;

public class InvalidBlockException extends RuntimeException {
    public InvalidBlockException(String message) {
        super(message);
    }

    public InvalidBlockException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
