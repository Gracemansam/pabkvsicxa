package com.lamiplus_common_api.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "plugin")
public class PluginPermissionConfig {
    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private String pluginType;
    private boolean autoLoad;
    private List<PermissionDefinition> permissions = new ArrayList<>();

    @Data
    public static class PermissionDefinition {
        private String permissionCode;
        private String name;
        private String description;
        private String resourceType;
        private String actionType;
        private String endpoint;
        private String permissionExpression;
    }
}