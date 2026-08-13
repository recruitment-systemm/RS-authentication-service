package org.example.authenticationservice.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.authenticationservice.dto.response.ErrorDetails;
import org.example.authenticationservice.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<ValidationError> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Validation failed", new ErrorDetails("VALIDATION_ERROR", errors)));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(ResourceAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("RESOURCE_ALREADY_EXISTS", null)));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("RESOURCE_NOT_FOUND", null)));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("INVALID_CREDENTIALS", null)));
    }

    @ExceptionHandler(OrganizationNotAcceptedException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationNotAccepted(OrganizationNotAcceptedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("ORGANIZATION_NOT_ACCEPTED", null)));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUpload(FileUploadException exception) {
        log.error("File upload failed", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "Failed to upload document", new ErrorDetails("FILE_UPLOAD_ERROR", null)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "Internal server error", new ErrorDetails("INTERNAL_SERVER_ERROR", null)));
    }

    @ExceptionHandler(InvalidOrganizationStatusException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationStatus(InvalidOrganizationStatusException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("ORGANIZATION_STATUS_ERROR", null)));
    }
}