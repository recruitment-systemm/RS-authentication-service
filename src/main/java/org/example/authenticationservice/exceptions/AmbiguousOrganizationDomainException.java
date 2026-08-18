package org.example.authenticationservice.exceptions;

public class AmbiguousOrganizationDomainException extends RuntimeException {

    public AmbiguousOrganizationDomainException(String message) {
        super(message);
    }
}
