package org.example.authenticationservice.dto.response;

public record ErrorResponse(boolean success, String message, ErrorDetails error) { }