package com.lamiplus_common_api.api;

public class PluginInitializationException extends RuntimeException {
    public PluginInitializationException(String message) {
        super(message);
    }

    public PluginInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}