package com.lamiplus_common_api.api;


import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface that all plugins must implement
 */
public interface Plugin {

    void initialize(PluginInfo info);

    void start();

    void stop();

    String getDescription();

    String getVersion();

    String getAuthor();

    String getName();

    List<String> getDependencies();


    default String getBasePath() {
        return getPluginId().toLowerCase();
    }

    default List<PluginPermission> getSecurityPermissions() {
        return new ArrayList<>();
    }

    default List<String> getRequiredRoles() {
        return new ArrayList<>();
    }

    default List<String> getAuditableActions() {
        return new ArrayList<>();
    }

    default List<String> getAuditableResourceTypes() {
        return new ArrayList<>();
    }

    default Logger getLogger() {
        return new PluginLogger(getPluginId());
    }

    default String getPluginId() {
        return "unknown";
    }

    default PluginType getPluginType() {
        return PluginType.SERVICE;
    }

    default boolean isAutoLoad() {
        return getPluginType() == PluginType.BASE;
    }
}