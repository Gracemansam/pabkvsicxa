package com.lamiplus_common_api.api;


import java.util.Optional;
import java.util.List;

/**
 * Registry for plugin data services.
 * Allows plugins to register and lookup data services.
 */
public interface PluginDataServiceRegistry {

    /**
     * Register a data service
     * @param pluginId - the plugin that owns this service
     * @param entityName - entity name (e.g., "Diagnosis")
     * @param service - the service implementation
     */
    void register(String pluginId, String entityName, PluginDataService service);

    /**
     * Get service by entity name (searches all plugins)
     */
    Optional<PluginDataService> getServiceByEntity(String entityName);

    /**
     * Get service by plugin ID and entity name
     */
    Optional<PluginDataService> getService(String pluginId, String entityName);

    /**
     * Get all services from a plugin
     */
    List<PluginDataService> getPluginServices(String pluginId);

    /**
     * Check if service exists
     */
    boolean hasService(String entityName);

    /**
     * Check if service exists in specific plugin
     */
    boolean hasService(String pluginId, String entityName);
}
