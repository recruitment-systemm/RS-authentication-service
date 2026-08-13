package org.example.authenticationservice.exceptions;

public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(String message) {
        super(message);
    }
}