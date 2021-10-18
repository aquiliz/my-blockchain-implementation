package com.aquiliz.blockchain.exception;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
