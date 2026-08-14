package org.example.authenticationservice.exceptions;

public class InvalidEmployeeEmailException extends RuntimeException {

    public InvalidEmployeeEmailException(String message) {
        super(message);
    }
}