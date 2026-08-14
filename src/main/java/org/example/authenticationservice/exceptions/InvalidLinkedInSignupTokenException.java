package org.example.authenticationservice.exceptions;

public class InvalidLinkedInSignupTokenException extends RuntimeException {

    public InvalidLinkedInSignupTokenException(String message) {
        super(message);
    }
}
