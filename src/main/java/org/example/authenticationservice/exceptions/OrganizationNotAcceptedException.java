package org.example.authenticationservice.exceptions;

public class OrganizationNotAcceptedException extends RuntimeException {

    public OrganizationNotAcceptedException(String message) {
        super(message);
    }
}