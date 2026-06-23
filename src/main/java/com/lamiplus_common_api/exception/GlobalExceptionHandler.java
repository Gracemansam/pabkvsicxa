package com.lamiplus_common_api.exception;

import com.lamiplus_common_api.api.PluginException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
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


@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;



    private String getRootCauseMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }


    private String cleanMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null) return "An unexpected error occurred";
        if (message.contains(": ")) {
            int colonIndex = message.indexOf(": ");
            String prefix = message.substring(0, colonIndex);
            if (!prefix.contains(" ")) {
                return message.substring(colonIndex + 2);
            }
        }
        return message;
    }

    private String extractFieldFromDuplicateKey(String msg) {
        if (msg == null) return null;
        if (msg.contains("Key (") && msg.contains(")=")) {
            int start = msg.indexOf("Key (") + 5;
            int end   = msg.indexOf(")", start);
            if (end > start) return msg.substring(start, end).trim();
        }
        return null;
    }

    private String extractFieldFromNullConstraint(String msg) {
        if (msg == null) return null;
        for (String q : new String[]{"column \"", "column '"}) {
            if (msg.contains(q)) {
                int start = msg.indexOf(q) + q.length();
                char close = q.endsWith("\"") ? '"' : '\'';
                int end = msg.indexOf(close, start);
                if (end > start) return msg.substring(start, end);
            }
        }
        return null;
    }

    private String extractFieldFromForeignKey(String msg) {
        if (msg == null) return null;
        if (msg.contains("on table \"")) {
            int start = msg.indexOf("on table \"") + 10;
            int end   = msg.indexOf("\"", start);
            if (end > start) return msg.substring(start, end);
        }
        return null;
    }

    // ============================================================================
    // BUSINESS & PLUGIN EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<List<ErrorModel>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        String codes = ex.getErrors().stream()
                .map(ErrorModel::getCode)
                .collect(Collectors.joining(", "));

        log.warn("Business exception at {}: codes=[{}]", request.getRequestURI(), codes);

        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(ex.getErrors(), status);
    }

    @ExceptionHandler(PluginException.class)
    public ResponseEntity<List<ErrorModel>> handlePluginException(
            PluginException ex, HttpServletRequest request) {

        log.error("Plugin exception at {}: {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                List.of(new ErrorModel("PLUGIN_ERROR", ex.getMessage(), null)),
                HttpStatus.BAD_REQUEST
        );
    }

    // ============================================================================
    // VALIDATION & INPUT EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorModel>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation failed at {}: {} error(s)",
                request.getRequestURI(), ex.getBindingResult().getErrorCount());

        List<ErrorModel> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorModel("VALIDATION_ERROR", fe.getDefaultMessage(), fe.getField()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<List<ErrorModel>> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing parameter at {}: {}", request.getRequestURI(), ex.getParameterName());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.MISSING_REQUIRED_FIELD
                        .toErrorModel("Missing required parameter: " + ex.getParameterName())),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<List<ErrorModel>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Type mismatch at {}: param={}, value={}",
                request.getRequestURI(), ex.getName(), ex.getValue());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.INVALID_INPUT_FORMAT
                        .toErrorModel("Invalid value for parameter: " + ex.getName())),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<ErrorModel>> handleInvalidJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Unreadable request body at {}", request.getRequestURI());

        String userMessage;
        Throwable cause = ex.getCause();

        // Jackson 3 (tools.jackson) — used by Spring Boot 4.x
        if (cause instanceof tools.jackson.databind.exc.InvalidFormatException ife) {
            String fieldName = ife.getPath().isEmpty() ? "unknown"
                    : ife.getPath().get(ife.getPath().size() - 1).getPropertyName();
            String badValue  = String.valueOf(ife.getValue());

            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String accepted = Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(Object::toString).collect(Collectors.joining(", "));
                userMessage = String.format(
                        "'%s' is not valid for '%s'. Accepted values: %s", badValue, fieldName, accepted);
            } else {
                userMessage = String.format("Invalid value '%s' for field '%s'", badValue, fieldName);
            }
        } else if (cause instanceof tools.jackson.core.exc.StreamReadException) {
            userMessage = "The request body contains malformed JSON. Please check the format and try again.";
        } else {
            userMessage = "The request body is missing or could not be read.";
        }

        return new ResponseEntity<>(
                List.of(new ErrorModel("INVALID_JSON", userMessage, null)),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<List<ErrorModel>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.INVALID_REQUEST.toErrorModel(cleanMessage(ex))),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<List<ErrorModel>> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {

        log.warn("Illegal state at {}: {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(
                List.of(new ErrorModel("CONFLICT", cleanMessage(ex), null)),
                HttpStatus.CONFLICT
        );
    }


    @ExceptionHandler(SQLGrammarException.class)
    public ResponseEntity<List<ErrorModel>> handleSQLGrammar(
            SQLGrammarException ex, HttpServletRequest request) {

        // Log full details for the DBA/developer
        log.error("SQLGrammarException at {} — possible missing table or schema migration. SQL state: {}. Cause: {}",
                request.getRequestURI(), ex.getSQLState(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(new ErrorModel("SERVICE_UNAVAILABLE",
                        "This feature is not available right now. Please try again later or contact support.", null)),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    /**
     * Duplicate key / unique constraint.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ErrorModel>> handleConstraint(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.error("ConstraintViolationException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        String rootCause = getRootCauseMessage(ex);
        String userMessage;
        String field = null;
        String code  = "DUPLICATE_RESOURCE";

        if (rootCause != null && rootCause.contains("duplicate key")) {
            field = extractFieldFromDuplicateKey(rootCause);
            if (rootCause.contains("app_users_email_key")) {
                field = "email";
                userMessage = "A user with this email address already exists.";
            } else if (rootCause.contains("app_users_user_name_key")) {
                field = "username";
                userMessage = "A user with this username already exists.";
            } else if (rootCause.contains("app_users_phone_number_key")) {
                field = "phoneNumber";
                userMessage = "A user with this phone number already exists.";
            } else {
                userMessage = field != null
                        ? String.format("A record with this %s already exists.", field)
                        : "This record already exists.";
            }
        } else {
            code        = "BUSINESS_RULE_VIOLATION";
            userMessage = "This operation cannot be completed due to a data conflict. Please review your input.";
        }

        ErrorModel error = field != null
                ? new ErrorModel(code, userMessage, field)
                : new ErrorModel(code, userMessage);

        HttpStatus status = "DUPLICATE_RESOURCE".equals(code) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(List.of(error), status);
    }

    /**
     * Data too long, bad value for column type, etc.
     */
    @ExceptionHandler(DataException.class)
    public ResponseEntity<List<ErrorModel>> handleDataException(
            DataException ex, HttpServletRequest request) {

        log.error("DataException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(new ErrorModel("INVALID_DATA",
                        "One or more values provided are invalid. Please check your input and try again.", null)),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Database lock timeout.
     */
    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<List<ErrorModel>> handleLockAcquisition(
            LockAcquisitionException ex, HttpServletRequest request) {

        log.error("LockAcquisitionException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(new ErrorModel("SERVICE_UNAVAILABLE",
                        "The system is currently busy. Please try again in a moment.", null)),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    /**
     * Cannot reach the database.
     */
    @ExceptionHandler(JDBCConnectionException.class)
    public ResponseEntity<List<ErrorModel>> handleJDBCConnection(
            JDBCConnectionException ex, HttpServletRequest request) {

        log.error("JDBCConnectionException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(new ErrorModel("SERVICE_UNAVAILABLE",
                        "The service is temporarily unavailable. Please try again shortly.", null)),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    /**
     * Query timeout.
     */
    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<List<ErrorModel>> handleQueryTimeout(
            QueryTimeoutException ex, HttpServletRequest request) {

        log.error("QueryTimeoutException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(new ErrorModel("SERVICE_UNAVAILABLE",
                        "The request took too long to process. Please try again.", null)),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<List<ErrorModel>> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("DataIntegrityViolationException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        String rootCause = getRootCauseMessage(ex);
        String userMessage;
        String field = null;
        String code  = "DUPLICATE_RESOURCE";
        HttpStatus status = HttpStatus.CONFLICT;

        if (rootCause != null && rootCause.contains("duplicate key")) {
            field = extractFieldFromDuplicateKey(rootCause);

            if (rootCause.contains("app_users_email_key")) {
                field = "email";
                userMessage = "A user with this email address already exists.";
            } else if (rootCause.contains("app_users_user_name_key")) {
                field = "username";
                userMessage = "A user with this username already exists.";
            } else if (rootCause.contains("app_users_phone_number_key")) {
                field = "phoneNumber";
                userMessage = "A user with this phone number already exists.";
            } else {
                userMessage = field != null
                        ? String.format("A record with this %s already exists.", field)
                        : "This record already exists in the system.";
            }

        } else if (rootCause != null && rootCause.contains("violates foreign key constraint")) {
            code      = "REFERENCE_ERROR";
            status    = HttpStatus.BAD_REQUEST;
            field     = extractFieldFromForeignKey(rootCause);
            userMessage = field != null
                    ? String.format("The referenced %s could not be found. Please check your selection.", field)
                    : "This operation references data that no longer exists.";

        } else if (rootCause != null && rootCause.contains("violates not-null constraint")) {
            code      = "MISSING_REQUIRED_FIELD";
            status    = HttpStatus.BAD_REQUEST;
            field     = extractFieldFromNullConstraint(rootCause);
            userMessage = field != null
                    ? String.format("The field '%s' is required.", field)
                    : "A required field is missing.";

        } else {
            code      = "DATA_ERROR";
            status    = HttpStatus.BAD_REQUEST;
            userMessage = "The data provided conflicts with existing records. Please review your input.";
        }

        ErrorModel error = field != null
                ? new ErrorModel(code, userMessage, field)
                : new ErrorModel(code, userMessage);

        return new ResponseEntity<>(List.of(error), status);
    }

    /**
     * Catch-all for any remaining Spring DataAccessException subtype not handled above.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<List<ErrorModel>> handleDataAccess(
            DataAccessException ex, HttpServletRequest request) {

        // Log everything — nothing technical goes to the client
        log.error("DataAccessException [{}] at {}: {}",
                ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(new ErrorModel("SERVICE_UNAVAILABLE",
                        "A database error occurred. Please try again or contact support if the problem persists.", null)),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    // ============================================================================
    // SECURITY EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<List<ErrorModel>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Bad credentials at {}", request.getRequestURI());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.INVALID_CREDENTIALS.toErrorModel()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<List<ErrorModel>> handleLocked(
            LockedException ex, HttpServletRequest request) {

        log.warn("Account locked at {}", request.getRequestURI());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.ACCOUNT_LOCKED.toErrorModel()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<List<ErrorModel>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied at {}", request.getRequestURI());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.INSUFFICIENT_PRIVILEGES
                        .toErrorModel("You do not have permission to perform this action.")),
                HttpStatus.FORBIDDEN
        );
    }

    // ============================================================================
    // ROUTING & HTTP METHOD EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest request) throws IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/")) {
            log.warn("API endpoint not found: {}", path);
            return new ResponseEntity<>(
                    List.of(StandardErrorCodes.ENDPOINT_NOT_FOUND
                            .toErrorModel("The requested endpoint does not exist.")),
                    HttpStatus.NOT_FOUND
            );
        }

        // SPA fallback — serve index.html for client-side routes
        try {
            Resource index = new ClassPathResource("static/index.html");
            if (index.exists()) {
                String content = StreamUtils.copyToString(index.getInputStream(), StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .body(content);
            }
        } catch (IOException e) {
            log.error("Failed to load index.html", e);
        }

        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/").build();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<List<ErrorModel>> handleNoHandler(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("No handler at {}: {} {}", request.getRequestURI(), ex.getHttpMethod(), ex.getRequestURL());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.ENDPOINT_NOT_FOUND.toErrorModel("Endpoint not found.")),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<List<ErrorModel>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Method not supported at {}: {}", request.getRequestURI(), ex.getMethod());

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.METHOD_NOT_ALLOWED.toErrorModel("HTTP method not allowed.")),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }



    @ExceptionHandler(ServletException.class)
    public ResponseEntity<List<ErrorModel>> handleServlet(
            ServletException ex, HttpServletRequest request) {

        // Unwrap and delegate if a known DB exception is wrapped inside
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof DataIntegrityViolationException div) {
                return handleDataIntegrity(div, request);
            }
            if (cause instanceof SQLGrammarException sqle) {
                return handleSQLGrammar(sqle, request);
            }
            if (cause instanceof DataAccessException dae) {
                return handleDataAccess(dae, request);
            }
        }

        log.error("ServletException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.INTERNAL_ERROR.toErrorModel(
                        "An error occurred while processing your request. Please try again.")),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<List<ErrorModel>> handleNullPointer(
            NullPointerException ex, HttpServletRequest request) {

        log.error("NullPointerException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.INTERNAL_ERROR.toErrorModel(
                        "An unexpected error occurred. Please try again or contact support.")),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<ErrorModel>> handleAll(
            Exception ex, HttpServletRequest request) {

        // Unwrap wrapped DB exceptions before giving up
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof DataIntegrityViolationException div) {
                return handleDataIntegrity(div, request);
            }
            if (cause instanceof SQLGrammarException sqle) {
                return handleSQLGrammar(sqle, request);
            }
            if (cause instanceof DataAccessException dae) {
                return handleDataAccess(dae, request);
            }
        }

        log.error("Unhandled exception [{}] at {}: {}",
                ex.getClass().getName(), request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                List.of(StandardErrorCodes.INTERNAL_ERROR.toErrorModel(
                        "An unexpected error occurred. Please try again or contact support.")),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}