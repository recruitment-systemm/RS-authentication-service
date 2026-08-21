package org.example.authenticationservice.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.example.authenticationservice.dto.response.ErrorDetails;
import org.example.authenticationservice.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindingValidation(BindException exception) {
        List<ValidationError> errors = exception
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(this::toValidationError)
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
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("FILE_UPLOAD_ERROR", null)));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exception) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(false, "File is too large", new ErrorDetails("FILE_TOO_LARGE", null)));
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

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordResetToken(InvalidPasswordResetTokenException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("INVALID_PASSWORD_RESET_TOKEN", null)));
    }

    @ExceptionHandler(InvalidLinkedInSignupTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLinkedInSignupToken(InvalidLinkedInSignupTokenException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("INVALID_LINKEDIN_SIGNUP_TOKEN", null)));
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationNotFound(OrganizationNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("ORGANIZATION_NOT_FOUND", null)));
    }

    @ExceptionHandler(AmbiguousOrganizationDomainException.class)
    public ResponseEntity<ErrorResponse> handleAmbiguousOrganizationDomain(AmbiguousOrganizationDomainException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("AMBIGUOUS_ORGANIZATION_DOMAIN", null)));
    }

    private ValidationError toValidationError(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return new ValidationError(error.getObjectName(), error.getDefaultMessage());
    }

    @ExceptionHandler(InvalidEmployeeEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmployeeEmail(InvalidEmployeeEmailException ex) {
        ErrorResponse response = new ErrorResponse(false, ex.getMessage(), new ErrorDetails("INVALID_EMPLOYEE_EMAIL", null));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException exception) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(false, exception.getMessage(), new ErrorDetails("RATE_LIMIT_EXCEEDED", null)));
    }
}
