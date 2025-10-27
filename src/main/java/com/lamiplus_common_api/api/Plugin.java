package com.lamiplus_common_api.api;

import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Plugin {

    String getPluginId();

    String getName();

    String getVersion();

    String getDescription();

    String getAuthor();

    default PluginType getPluginType() {
        return PluginType.SERVICE;
    }

    void initialize(PluginInfo info);

    void start();

    void stop();

    default boolean isHealthy() {
        return true;
    }

    default String getHealthStatus() {
        return isHealthy() ? "Healthy" : "Unhealthy";
    }

    List<String> getDependencies();

    default Map<String, Object> getMetadata() {
        return Map.of();
    }

    default List<PluginPermission> getSecurityPermissions() {
        return List.of();
    }

    default List<String> getRequiredRoles() {
        return List.of();
    }

    default List<String> getAuditableActions() {
        return List.of();
    }

    default List<String> getAuditableResourceTypes() {
        return List.of();
    }

    default String getBasePath() {
        return getPluginId().toLowerCase();
    }

    default boolean isAutoLoad() {
        return getPluginType() == PluginType.BASE;
    }

    default Logger getLogger() {
        return new PluginLogger(getPluginId());
    }

    default Object execute(String operation, Map<String, Object> parameters) throws PluginException {
        throw new PluginException(
                getPluginId(),
                PluginException.PluginErrorCode.EXECUTION_FAILED,
                "Operation not supported: " + operation
        );
    }


    <T> Optional<T> getService(Class<T> serviceClass);


    <T> Optional<T> getService(String serviceName, Class<T> serviceClass);


    default boolean hasService(Class<?> serviceClass) {
        return getService(serviceClass).isPresent();
    }
}