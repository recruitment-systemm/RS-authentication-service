package org.example.authenticationservice.exceptions;

public record ValidationError(String field, String message) { }