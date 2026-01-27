package com.lamiplus_common_api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorModel {
    private String code;
    private String message;
    private String field;

    public ErrorModel(String code, String message) {
        this.code = code;
        this.message = message;
    }


    public ErrorModel(StandardErrorCodes errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public ErrorModel(StandardErrorCodes errorCode, String field) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.field = field;
    }
}