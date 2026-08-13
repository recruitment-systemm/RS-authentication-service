package org.example.authenticationservice.exceptions;

public class FileUploadException extends RuntimeException {

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileUploadException(String message) {
        super(message);
    }
}