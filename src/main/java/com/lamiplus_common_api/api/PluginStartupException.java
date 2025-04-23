package com.lamiplus_common_api.api;

public class PluginStartupException extends RuntimeException {
    public PluginStartupException(String message) {
        super(message);
    }

    public PluginStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}