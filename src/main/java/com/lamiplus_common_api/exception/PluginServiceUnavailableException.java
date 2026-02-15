package com.lamiplus_common_api.exception;


public class PluginServiceUnavailableException extends RuntimeException {

    private final String entityName;

    public PluginServiceUnavailableException(String entityName) {
        super("Plugin service not available for entity: " + entityName);
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }
}