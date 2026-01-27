package com.lamiplus_common_api.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.util.StreamUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;



    private boolean isDevEnvironment() {
        return "dev".equals(activeProfile) || "local".equals(activeProfile) || log.isDebugEnabled();
    }

    private String getRootCauseMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    private String extractConstraintName(ConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("constraint")) {
            return message.substring(0, Math.min(message.length(), 200));
        }
        return "Unknown constraint";
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<List<ErrorModel>> handleBusinessException(
            BusinessException bex,
            HttpServletRequest request) {

        String errorCodes = bex.getErrors().stream()
                .map(ErrorModel::getCode)
                .collect(Collectors.joining(", "));

        if (log.isDebugEnabled()) {
            log.debug("Business exception at {}: codes=[{}], details={}",
                    request.getRequestURI(), errorCodes, bex.getErrors());
        } else {
            log.warn("Business exception at {}: codes=[{}]",
                    request.getRequestURI(), errorCodes);
        }

        HttpStatus status = (bex.getHttpStatus() != null) ? bex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(bex.getErrors(), status);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorModel>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation failed at {}: {} errors", request.getRequestURI(),
                ex.getBindingResult().getErrorCount());

        List<ErrorModel> errorModelList = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Validation error: field={}, message={}", fe.getField(), fe.getDefaultMessage());
                    }
                    return new ErrorModel("VALIDATION_ERROR", fe.getDefaultMessage(), fe.getField());
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(errorModelList, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<List<ErrorModel>> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn("Missing required parameter at {}: {} of type {}",
                request.getRequestURI(), ex.getParameterName(), ex.getParameterType());

        String message = isDevEnvironment()
                ? String.format("Missing required parameter: %s (%s)", ex.getParameterName(), ex.getParameterType())
                : "Missing required parameter: " + ex.getParameterName();

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.MISSING_REQUIRED_FIELD.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<List<ErrorModel>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch at {}: parameter={}, value={}",
                request.getRequestURI(), ex.getName(), ex.getValue());

        String message = isDevEnvironment()
                ? String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName())
                : "Invalid parameter value: " + ex.getName();

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INVALID_INPUT_FORMAT.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<ErrorModel>> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Invalid JSON request at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = isDevEnvironment()
                ? "Invalid JSON: " + ex.getMostSpecificCause().getMessage()
                : "Invalid request format";


        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INVALID_JSON.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<List<ErrorModel>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Invalid argument at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = isDevEnvironment() ? ex.getMessage() : "Invalid argument provided";
        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INVALID_REQUEST.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // ============================================================================
    // DATABASE EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ErrorModel>> handleConstraintException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.error("Constraint violation at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        String rootCause = getRootCauseMessage(ex);
        String userMessage;
        String field = null;

        // Check for specific constraint violations
        if (rootCause != null && rootCause.contains("duplicate key")) {
            if (rootCause.contains("app_users_email_key")) {
                field = "email";
                userMessage = "A user with this email address already exists";
            } else if (rootCause.contains("app_users_user_name_key")) {
                field = "username";
                userMessage = "A user with this username already exists";
            } else if (rootCause.contains("app_users_phone_number_key")) {
                field = "phoneNumber";
                userMessage = "A user with this phone number already exists";
            } else {
                String constraintName = extractConstraintFromError(rootCause);
                userMessage = isDevEnvironment()
                        ? "Duplicate entry: " + constraintName
                        : "This record already exists";
            }
        } else {
            String constraintName = extractConstraintName(ex);
            userMessage = isDevEnvironment()
                    ? "Database constraint violation: " + constraintName
                    : "Operation violates database constraints";
        }

        ErrorModel error = field != null
                ? new ErrorModel("DUPLICATE_RESOURCE", userMessage, field)
                : StandardErrorCodes.BUSINESS_RULE_VIOLATION.toErrorModel(userMessage);

        List<ErrorModel> errors = List.of(error);

        return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<List<ErrorModel>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.error("=== DataIntegrityViolationException Handler Called ===");
        log.error("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        String rootCause = getRootCauseMessage(ex);
        String userMessage;
        String field = null;
        String errorCode = "DUPLICATE_RESOURCE";

        // Extract duplicate key information
        if (rootCause != null && rootCause.contains("duplicate key")) {

            // First extract the field from the Key (field)= pattern
            field = extractFieldFromDuplicateKey(rootCause);

            // Then check for specific constraints to provide better messages
            if (rootCause.contains("app_users_email_key") || (field != null && field.equalsIgnoreCase("email"))) {
                field = "email";
                userMessage = "A user with this email address already exists";
            } else if (rootCause.contains("app_users_user_name_key") || (field != null && field.equalsIgnoreCase("user_name"))) {
                field = "username";
                userMessage = "A user with this username already exists";
            } else if (rootCause.contains("app_users_phone_number_key") || (field != null && field.equalsIgnoreCase("phone_number"))) {
                field = "phoneNumber";
                userMessage = "A user with this phone number already exists";
            } else if (field != null) {
                // Generic message using extracted field
                userMessage = String.format("A record with this %s already exists", field);
            } else {
                // Fallback
                String constraintName = extractConstraintFromError(rootCause);
                userMessage = isDevEnvironment()
                        ? "Duplicate entry: " + constraintName
                        : "This record already exists in the system";
            }
        } else if (rootCause != null && rootCause.contains("violates foreign key constraint")) {
            errorCode = "FOREIGN_KEY_VIOLATION";
            field = extractFieldFromForeignKey(rootCause);
            userMessage = field != null
                    ? String.format("The referenced %s does not exist", field)
                    : "Cannot perform this operation because it references data that doesn't exist";
        } else if (rootCause != null && rootCause.contains("violates not-null constraint")) {
            errorCode = "NULL_VALUE_NOT_ALLOWED";
            field = extractFieldFromNullConstraint(rootCause);
            userMessage = field != null
                    ? String.format("Field '%s' is required and cannot be empty", field)
                    : "Required field is missing";
        } else {
            errorCode = "DATA_INTEGRITY_VIOLATION";
            userMessage = isDevEnvironment()
                    ? "Database constraint violation: " + rootCause
                    : "This operation violates database constraints";
        }

        ErrorModel error = field != null
                ? new ErrorModel(errorCode, userMessage, field)
                : new ErrorModel(errorCode, userMessage);

        List<ErrorModel> errors = List.of(error);

        HttpStatus status = errorCode.equals("DUPLICATE_RESOURCE") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errors, status);
    }

    private String extractConstraintFromError(String errorMessage) {
        if (errorMessage == null) return "unknown";

        // Try to extract constraint name between quotes
        if (errorMessage.contains("constraint \"")) {
            int start = errorMessage.indexOf("constraint \"") + 12;
            int end = errorMessage.indexOf("\"", start);
            if (end > start) {
                return errorMessage.substring(start, end);
            }
        }

        // Alternative format
        if (errorMessage.contains("constraint '")) {
            int start = errorMessage.indexOf("constraint '") + 12;
            int end = errorMessage.indexOf("'", start);
            if (end > start) {
                return errorMessage.substring(start, end);
            }
        }

        return "unknown";
    }

    private String extractFieldFromDuplicateKey(String errorMessage) {
        if (errorMessage == null) return null;

        // Try to extract field from "Key (field)=" pattern
        if (errorMessage.contains("Key (") && errorMessage.contains(")=")) {
            int start = errorMessage.indexOf("Key (") + 5;
            int end = errorMessage.indexOf(")", start);
            if (end > start) {
                return errorMessage.substring(start, end).trim();
            }
        }

        return null;
    }

    private String extractFieldFromNullConstraint(String errorMessage) {
        if (errorMessage == null) return null;

        // Try to extract field from not-null constraint message
        if (errorMessage.contains("column \"")) {
            int start = errorMessage.indexOf("column \"") + 8;
            int end = errorMessage.indexOf("\"", start);
            if (end > start) {
                return errorMessage.substring(start, end);
            }
        }

        // Alternative format
        if (errorMessage.contains("column '")) {
            int start = errorMessage.indexOf("column '") + 8;
            int end = errorMessage.indexOf("'", start);
            if (end > start) {
                return errorMessage.substring(start, end);
            }
        }

        return null;
    }

    private String extractFieldFromForeignKey(String errorMessage) {
        if (errorMessage == null) return null;

        // Try to extract referenced table/field
        if (errorMessage.contains("on table \"")) {
            int start = errorMessage.indexOf("on table \"") + 10;
            int end = errorMessage.indexOf("\"", start);
            if (end > start) {
                return errorMessage.substring(start, end);
            }
        }

        return null;
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<List<ErrorModel>> handleDataAccess(
            DataAccessException ex,
            HttpServletRequest request) {

        log.error("Database error at {}", request.getRequestURI(), ex);

        String rootCause = getRootCauseMessage(ex);
        String message = isDevEnvironment()
                ? "Database operation failed: " + (rootCause != null ? rootCause : ex.getMessage())
                : "Database operation failed";

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.DATABASE_ERROR.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ============================================================================
    // SECURITY EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<List<ErrorModel>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Bad credentials at {}: {}", request.getRequestURI(), ex.getMessage());

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INVALID_CREDENTIALS.toErrorModel()
        );

        return new ResponseEntity<>(errors, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<List<ErrorModel>> handleLockedException(
            LockedException ex,
            HttpServletRequest request) {

        log.warn("Account locked at {}: {}", request.getRequestURI(), ex.getMessage());

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.ACCOUNT_LOCKED.toErrorModel()
        );

        return new ResponseEntity<>(errors, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<List<ErrorModel>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INSUFFICIENT_PRIVILEGES.toErrorModel("You don't have permission to perform this action")
        );

        return new ResponseEntity<>(errors, HttpStatus.FORBIDDEN);
    }

    // ============================================================================
    // ROUTING & HTTP METHOD EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) throws IOException {

        String requestPath = request.getRequestURI();

        if (requestPath.startsWith("/api/")) {
            log.warn("API endpoint not found: {}", requestPath);

            List<ErrorModel> errors = List.of(
                    StandardErrorCodes.ENDPOINT_NOT_FOUND.toErrorModel("API endpoint not found: " + requestPath)
            );

            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        }

        try {
            Resource indexResource = new ClassPathResource("static/index.html");
            if (indexResource.exists()) {
                String content = StreamUtils.copyToString(
                        indexResource.getInputStream(),
                        StandardCharsets.UTF_8
                );
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.TEXT_HTML)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .body(content);
            }
        } catch (IOException e) {
            log.error("Failed to load index.html", e);
        }

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/")
                .build();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<List<ErrorModel>> handleNoHandlerFound(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        log.warn("No handler found at {}: {} {}", request.getRequestURI(), ex.getHttpMethod(), ex.getRequestURL());

        String message = isDevEnvironment()
                ? String.format("No handler for %s %s", ex.getHttpMethod(), ex.getRequestURL())
                : "Endpoint not found";

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.ENDPOINT_NOT_FOUND.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<List<ErrorModel>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("Method not supported at {}: {}", request.getRequestURI(), ex.getMethod());

        String message = isDevEnvironment()
                ? String.format("Method %s not supported. Supported methods: %s",
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()))
                : "HTTP method not allowed";

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.METHOD_NOT_ALLOWED.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // ============================================================================
    // NULL POINTER & CATCH-ALL EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<List<ErrorModel>> handleServletException(
            ServletException ex,
            HttpServletRequest request) {

        log.error("=== ServletException Handler Called ===");
        log.error("ServletException at {}: {}", request.getRequestURI(), ex.getMessage());

        // Check if it's wrapping a DataIntegrityViolationException
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
            log.error("Checking cause: {}", rootCause.getClass().getName());
            if (rootCause instanceof DataIntegrityViolationException) {
                log.error("Found wrapped DataIntegrityViolationException!");
                return handleDataIntegrityViolation((DataIntegrityViolationException) rootCause, request);
            }
        }

        // Not a data integrity issue, treat as generic error
        String message = isDevEnvironment()
                ? "Request processing failed: " + ex.getMessage()
                : "An error occurred processing your request";

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INTERNAL_ERROR.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<List<ErrorModel>> handleNullPointer(
            NullPointerException ex,
            HttpServletRequest request) {

        log.error("NullPointerException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        String message = isDevEnvironment()
                ? "Null pointer error: " + getRootCauseMessage(ex)
                : "An internal error occurred";

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INTERNAL_ERROR.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<ErrorModel>> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("=== Generic Exception Handler Called ===");
        log.error("Exception type: {}", ex.getClass().getName());
        log.error("Unhandled exception at {}: {} - {}",
                request.getRequestURI(),
                ex.getClass().getName(),
                ex.getMessage(),
                ex);

        // Check if it's a wrapped DataIntegrityViolationException
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
            if (rootCause instanceof DataIntegrityViolationException) {
                return handleDataIntegrityViolation((DataIntegrityViolationException) rootCause, request);
            }
        }

        String message = isDevEnvironment()
                ? String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage())
                : "An unexpected error occurred";

        List<ErrorModel> errors = List.of(
                StandardErrorCodes.INTERNAL_ERROR.toErrorModel(message)
        );

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}