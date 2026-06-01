package com.lamiplus_common_api.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j

public class DefaultPluginDataServiceRegistry implements PluginDataServiceRegistry {

    // pluginId -> (entityName -> service)
    private final Map<String, Map<String, PluginDataService>> registry = new ConcurrentHashMap<>();

    // entityName -> service (fast lookup)
    private final Map<String, PluginDataService> entityIndex = new ConcurrentHashMap<>();

    @Override
    public void register(String pluginId, String entityName, PluginDataService service) {
        registry.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>())
                .put(entityName, service);

        PluginDataService existing = entityIndex.put(entityName, service);
        if (existing != null) {
            log.warn("Replaced existing service for [{}/{}]: {} -> {}",
                    pluginId, entityName,
                    existing.getClass().getSimpleName(),
                    service.getClass().getSimpleName());
        } else {
            log.info("Registered plugin service [{}/{}]: {}",
                    pluginId, entityName, service.getClass().getSimpleName());
        }
    }

    @Override
    public void unregister(String entityName) {
        entityIndex.remove(entityName);
        registry.values().forEach(map -> map.remove(entityName));
        log.info("Unregistered plugin service for entity '{}'", entityName);
    }

    @Override
    public Optional<PluginDataService> getServiceByEntity(String entityName) {
        return Optional.ofNullable(entityIndex.get(entityName));
    }

    @Override
    public Optional<PluginDataService> getService(String pluginId, String entityName) {
        Map<String, PluginDataService> pluginServices = registry.get(pluginId);
        if (pluginServices == null) return Optional.empty();
        return Optional.ofNullable(pluginServices.get(entityName));
    }

    @Override
    public List<PluginDataService> getPluginServices(String pluginId) {
        Map<String, PluginDataService> pluginServices = registry.get(pluginId);
        if (pluginServices == null) return Collections.emptyList();
        return new ArrayList<>(pluginServices.values());
    }

    @Override
    public boolean hasService(String entityName) {
        return entityIndex.containsKey(entityName);
    }

    @Override
    public boolean hasService(String pluginId, String entityName) {
        Map<String, PluginDataService> pluginServices = registry.get(pluginId);
        return pluginServices != null && pluginServices.containsKey(entityName);
    }

    /**
     * Called by the plugin manager when a plugin JAR is unloaded.
     * Not in the interface — called directly by PluginLifecycleManager.
     */
    public void unregisterPlugin(String pluginId) {
        Map<String, PluginDataService> removed = registry.remove(pluginId);
        if (removed != null) {
            removed.keySet().forEach(entityIndex::remove);
            log.info("Unregistered all services for plugin: {}", pluginId);
        }
    }
}