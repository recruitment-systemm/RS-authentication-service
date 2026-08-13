package org.example.authenticationservice.exceptions;

public class InvalidOrganizationStatusException extends RuntimeException {
    public InvalidOrganizationStatusException(String message) {
        super(message);
    }
}