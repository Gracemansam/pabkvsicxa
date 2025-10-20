package com.lamiplus_common_api.api;


public class PluginException extends Exception {

    private final String pluginId;
    private final PluginErrorCode errorCode;



    public PluginException(String message) {
        super(message);
        this.pluginId = null;
        this.errorCode = PluginErrorCode.UNKNOWN;
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
        this.pluginId = null;
        this.errorCode = PluginErrorCode.UNKNOWN;
    }

    public PluginException(String pluginId, String message) {
        super(message);
        this.pluginId = pluginId;
        this.errorCode = PluginErrorCode.UNKNOWN;
    }

    public PluginException(String pluginId, String message, Throwable cause) {
        super(message, cause);
        this.pluginId = pluginId;
        this.errorCode = PluginErrorCode.UNKNOWN;
    }

    public PluginException(String pluginId, PluginErrorCode errorCode, String message) {
        super(message);
        this.pluginId = pluginId;
        this.errorCode = errorCode;
    }

    public PluginException(String pluginId, PluginErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.pluginId = pluginId;
        this.errorCode = errorCode;
    }


    public static PluginException initializationFailed(String pluginId, String message) {
        return new PluginException(pluginId, PluginErrorCode.INITIALIZATION_FAILED, message);
    }

    public static PluginException initializationFailed(String pluginId, String message, Throwable cause) {
        return new PluginException(pluginId, PluginErrorCode.INITIALIZATION_FAILED, message, cause);
    }


    public static PluginException startupFailed(String pluginId, String message) {
        return new PluginException(pluginId, PluginErrorCode.START_FAILED, message);
    }

    public static PluginException startupFailed(String pluginId, String message, Throwable cause) {
        return new PluginException(pluginId, PluginErrorCode.START_FAILED, message, cause);
    }



    public String getPluginId() {
        return pluginId;
    }

    public PluginErrorCode getErrorCode() {
        return errorCode;
    }

    public boolean isInitializationError() {
        return errorCode == PluginErrorCode.INITIALIZATION_FAILED;
    }

    public boolean isStartupError() {
        return errorCode == PluginErrorCode.START_FAILED;
    }

    public boolean isSecurityError() {
        return errorCode == PluginErrorCode.SECURITY_VIOLATION ||
                errorCode == PluginErrorCode.TENANT_NOT_AUTHORIZED;
    }


    public enum PluginErrorCode {
        UNKNOWN,
        NOT_FOUND,
        ALREADY_INSTALLED,
        INVALID_PLUGIN,
        MISSING_DEPENDENCIES,
        INITIALIZATION_FAILED,
        START_FAILED,
        STOP_FAILED,
        EXECUTION_FAILED,
        SECURITY_VIOLATION,
        CONFIGURATION_ERROR,
        DATABASE_ERROR,
        NETWORK_ERROR,
        TIMEOUT,
        CIRCUIT_BREAKER_OPEN,
        TENANT_NOT_AUTHORIZED,
        QUOTA_EXCEEDED,
        INVALID_CONFIGURATION,
        RESOURCE_NOT_FOUND,
        PERMISSION_DENIED
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PluginException");
        if (pluginId != null) {
            sb.append(" [").append(pluginId).append("]");
        }
        if (errorCode != null && errorCode != PluginErrorCode.UNKNOWN) {
            sb.append(" [").append(errorCode).append("]");
        }
        sb.append(": ").append(getMessage());
        return sb.toString();
    }
}