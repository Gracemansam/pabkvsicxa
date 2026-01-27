package com.lamiplus_common_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StandardErrorCodes implements ErrorCode {

    // Authentication & Authorization Errors
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "Access denied", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Authentication token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("TOKEN_INVALID", "Invalid authentication token", HttpStatus.UNAUTHORIZED),
    INSUFFICIENT_PRIVILEGES("INSUFFICIENT_PRIVILEGES", "Insufficient privileges to perform this action", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account has been locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Account has been disabled", HttpStatus.FORBIDDEN),

    // User Management Errors
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email address is already registered", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username is already taken", HttpStatus.CONFLICT),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "Invalid email format", HttpStatus.BAD_REQUEST),
    WEAK_PASSWORD("WEAK_PASSWORD", "Password does not meet security requirements", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Passwords do not match", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_INCORRECT("OLD_PASSWORD_INCORRECT", "Current password is incorrect", HttpStatus.BAD_REQUEST),
    USER_PROFILE_INCOMPLETE("USER_PROFILE_INCOMPLETE", "User profile is incomplete", HttpStatus.BAD_REQUEST),
    ACCOUNT_PENDING_VERIFICATION("ACCOUNT_PENDING_VERIFICATION", "Account is pending email verification", HttpStatus.FORBIDDEN),

    // Validation Errors
    VALIDATION_FAILED("VALIDATION_FAILED", "Input validation failed", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD("MISSING_REQUIRED_FIELD", "Required field is missing", HttpStatus.BAD_REQUEST),
    INVALID_INPUT_FORMAT("INVALID_INPUT_FORMAT", "Invalid input format", HttpStatus.BAD_REQUEST),
    INPUT_TOO_LONG("INPUT_TOO_LONG", "Input exceeds maximum length", HttpStatus.BAD_REQUEST),
    INPUT_TOO_SHORT("INPUT_TOO_SHORT", "Input is below minimum length", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT("INVALID_DATE_FORMAT", "Invalid date format", HttpStatus.BAD_REQUEST),
    FUTURE_DATE_NOT_ALLOWED("FUTURE_DATE_NOT_ALLOWED", "Future date is not allowed", HttpStatus.BAD_REQUEST),
    PAST_DATE_NOT_ALLOWED("PAST_DATE_NOT_ALLOWED", "Past date is not allowed", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER("INVALID_PHONE_NUMBER", "Invalid phone number format", HttpStatus.BAD_REQUEST),
    INVALID_POSTAL_CODE("INVALID_POSTAL_CODE", "Invalid postal code format", HttpStatus.BAD_REQUEST),

    // Resource Not Found Errors
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource not found", HttpStatus.NOT_FOUND),
    PAGE_NOT_FOUND("PAGE_NOT_FOUND", "Page not found", HttpStatus.NOT_FOUND),
    ENDPOINT_NOT_FOUND("ENDPOINT_NOT_FOUND", "API endpoint not found", HttpStatus.NOT_FOUND),
    FILE_NOT_FOUND("FILE_NOT_FOUND", "File not found", HttpStatus.NOT_FOUND),

    // Business Logic Errors
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "Business rule violation", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient account balance", HttpStatus.BAD_REQUEST),
    TRANSACTION_LIMIT_EXCEEDED("TRANSACTION_LIMIT_EXCEEDED", "Transaction limit exceeded", HttpStatus.BAD_REQUEST),
    DUPLICATE_TRANSACTION("DUPLICATE_TRANSACTION", "Duplicate transaction detected", HttpStatus.CONFLICT),
    OPERATION_NOT_ALLOWED("OPERATION_NOT_ALLOWED", "Operation not allowed in current state", HttpStatus.BAD_REQUEST),
    QUOTA_EXCEEDED("QUOTA_EXCEEDED", "Usage quota exceeded", HttpStatus.TOO_MANY_REQUESTS),
    RESOURCE_ALREADY_EXISTS("RESOURCE_ALREADY_EXISTS", "Resource already exists", HttpStatus.CONFLICT),
    RESOURCE_IN_USE("RESOURCE_IN_USE", "Resource is currently in use", HttpStatus.CONFLICT),

    // File & Upload Errors
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "File upload failed", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "File size exceeds maximum limit", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "Invalid file type", HttpStatus.BAD_REQUEST),
    CORRUPTED_FILE("CORRUPTED_FILE", "File is corrupted", HttpStatus.BAD_REQUEST),
    FILE_PROCESSING_ERROR("FILE_PROCESSING_ERROR", "Error processing file", HttpStatus.INTERNAL_SERVER_ERROR),

    // Database & System Errors
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CONNECTION_TIMEOUT("CONNECTION_TIMEOUT", "Connection timeout", HttpStatus.REQUEST_TIMEOUT),
    SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "System is under maintenance", HttpStatus.SERVICE_UNAVAILABLE),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    CONFIGURATION_ERROR("CONFIGURATION_ERROR", "System configuration error", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service error", HttpStatus.BAD_GATEWAY),

    // Rate Limiting & Security
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    SUSPICIOUS_ACTIVITY("SUSPICIOUS_ACTIVITY", "Suspicious activity detected", HttpStatus.FORBIDDEN),
    IP_BLOCKED("IP_BLOCKED", "IP address has been blocked", HttpStatus.FORBIDDEN),
    INVALID_API_KEY("INVALID_API_KEY", "Invalid API key", HttpStatus.UNAUTHORIZED),

    // Payment & Transaction Errors
    PAYMENT_FAILED("PAYMENT_FAILED", "Payment processing failed", HttpStatus.BAD_REQUEST),
    PAYMENT_DECLINED("PAYMENT_DECLINED", "Payment was declined", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_METHOD("INVALID_PAYMENT_METHOD", "Invalid payment method", HttpStatus.BAD_REQUEST),
    PAYMENT_EXPIRED("PAYMENT_EXPIRED", "Payment session has expired", HttpStatus.BAD_REQUEST),
    REFUND_FAILED("REFUND_FAILED", "Refund processing failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // Communication Errors
    EMAIL_SEND_FAILED("EMAIL_SEND_FAILED", "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    SMS_SEND_FAILED("SMS_SEND_FAILED", "Failed to send SMS", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_FAILED("NOTIFICATION_FAILED", "Failed to send notification", HttpStatus.INTERNAL_SERVER_ERROR),

    // General Errors
    INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("INVALID_REQUEST", "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_JSON("INVALID_JSON", "Invalid JSON format", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "HTTP method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION", "Operation not supported", HttpStatus.NOT_IMPLEMENTED),

    // Custom Application Specific Errors
    CUSTOM_BUSINESS_ERROR("CUSTOM_BUSINESS_ERROR", "Custom business rule violation", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    StandardErrorCodes(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public ErrorModel toErrorModel() {
        return new ErrorModel(this.code, this.message);
    }

    public ErrorModel toErrorModel(String customMessage) {
        return new ErrorModel(this.code, customMessage);
    }

    public ErrorModel toErrorModel(String customMessage, String field) {
        return new ErrorModel(this.code, customMessage, field);
    }

    public static StandardErrorCodes fromCode(String code) {
        for (StandardErrorCodes errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }

    public boolean isClientError() {
        return httpStatus.is4xxClientError();
    }

    public boolean isServerError() {
        return httpStatus.is5xxServerError();
    }
}