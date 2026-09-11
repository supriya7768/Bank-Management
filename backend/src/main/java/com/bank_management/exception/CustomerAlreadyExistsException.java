package com.bank_management.exception;

public class CustomerAlreadyExistsException extends RuntimeException{

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
