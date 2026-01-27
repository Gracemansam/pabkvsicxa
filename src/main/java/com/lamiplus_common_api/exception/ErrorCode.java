package com.lamiplus_common_api.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();

    default ErrorModel toErrorModel() {
        return new ErrorModel(getCode(), getMessage());
    }

    default ErrorModel toErrorModel(String customMessage) {
        return new ErrorModel(getCode(), customMessage);
    }

    default boolean isClientError() {
        return getHttpStatus().is4xxClientError();
    }

    default boolean isServerError() {
        return getHttpStatus().is5xxServerError();
    }
}
