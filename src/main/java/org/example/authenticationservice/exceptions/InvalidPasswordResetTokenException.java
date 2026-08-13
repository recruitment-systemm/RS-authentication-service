package org.example.authenticationservice.exceptions;

public class InvalidPasswordResetTokenException extends RuntimeException {

    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }
}