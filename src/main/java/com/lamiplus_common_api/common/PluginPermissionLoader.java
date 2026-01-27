package com.lamiplus_common_api.common;

import com.lamiplus_common_api.api.PluginPermission;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PluginPermissionLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginPermissionLoader.class);

    private final PluginPermissionConfig config;
    private List<PluginPermission> loadedPermissions;

    @Autowired
    public PluginPermissionLoader(PluginPermissionConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void loadPermissions() {
        logger.info("Loading permissions from configuration for plugin: {}", config.getId());

        this.loadedPermissions = config.getPermissions().stream()
                .map(this::mapToPluginPermission)
                .collect(Collectors.toList());

        logger.info("Successfully loaded {} permissions for plugin {}",
                loadedPermissions.size(), config.getId());
    }

    private PluginPermission mapToPluginPermission(PluginPermissionConfig.PermissionDefinition def) {
        return PluginPermission.builder()
                .pluginId(config.getId())
                .permissionCode(def.getPermissionCode())
                .name(def.getName())
                .description(def.getDescription())
                .resourceType(def.getResourceType())
                .actionType(def.getActionType())
                .endpoint(def.getEndpoint())
                .permissionExpression(def.getPermissionExpression())
                .build();
    }

    public List<PluginPermission> getPermissions() {
        return loadedPermissions;
    }
}