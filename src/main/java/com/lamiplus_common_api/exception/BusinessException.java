package com.lamiplus_common_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
public class BusinessException extends RuntimeException {
    private final List<ErrorModel> errors;
    private final HttpStatus httpStatus;


    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errors = Collections.singletonList(errorCode.toErrorModel());
        this.httpStatus = errorCode.getHttpStatus();
    }


    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errors = Collections.singletonList(errorCode.toErrorModel(customMessage));
        this.httpStatus = errorCode.getHttpStatus();
    }


    public BusinessException(List<ErrorModel> errors, HttpStatus httpStatus) {
        super(errors.isEmpty() ? "Business error" : errors.get(0).getMessage());
        this.errors = errors;
        this.httpStatus = httpStatus;
    }


    public BusinessException(ErrorCode... errorCodes) {
        super(errorCodes.length > 0 ? errorCodes[0].getMessage() : "Business error");
        this.errors = java.util.Arrays.stream(errorCodes)
                .map(ErrorCode::toErrorModel)
                .collect(java.util.stream.Collectors.toList());
        this.httpStatus = errorCodes.length > 0 ? errorCodes[0].getHttpStatus() : HttpStatus.BAD_REQUEST;
    }
}