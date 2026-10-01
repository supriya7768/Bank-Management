package com.bank_management.exception;

public class DuplicateIdempotencyKeyException extends RuntimeException{

    public DuplicateIdempotencyKeyException(String message) {
        super(message);
    }
}
