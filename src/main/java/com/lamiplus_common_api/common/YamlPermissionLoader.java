package com.lamiplus_common_api.common;

import com.lamiplus_common_api.api.PluginPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlPermissionLoader {
    private static final Logger logger = LoggerFactory.getLogger(YamlPermissionLoader.class);

    public static List<PluginPermission> loadPermissions(String yamlFile, ClassLoader classLoader) {
        List<PluginPermission> permissions = new ArrayList<>();

        if (classLoader == null) {
            logger.error("ClassLoader is null");
            return permissions;
        }

        try {
            logger.info("Attempting to load {} using classloader: {}",
                    yamlFile, classLoader.getClass().getName());

            InputStream inputStream = classLoader.getResourceAsStream(yamlFile);

            // Try alternative path if first attempt fails
            if (inputStream == null) {
                logger.warn("First attempt failed, trying with leading slash");
                inputStream = classLoader.getResourceAsStream("/" + yamlFile);
            }

            if (inputStream == null) {
                logger.error("Could not find YAML file: {} in classloader: {}",
                        yamlFile, classLoader);
                return permissions;
            }

            logger.info("Successfully found YAML file: {}", yamlFile);

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);

            Map<String, Object> pluginData = (Map<String, Object>) data.get("plugin");
            if (pluginData == null) {
                logger.error("No 'plugin' key found in YAML");
                return permissions;
            }

            String pluginId = (String) pluginData.get("id");
            List<Map<String, String>> permissionsList =
                    (List<Map<String, String>>) pluginData.get("permissions");

            if (permissionsList != null) {
                for (Map<String, String> permDef : permissionsList) {
                    PluginPermission permission = PluginPermission.builder()
                            .pluginId(pluginId)
                            .permissionCode(permDef.get("permissionCode"))
                            .name(permDef.get("name"))
                            .description(permDef.get("description"))
                            .resourceType(permDef.get("resourceType"))
                            .actionType(permDef.get("actionType"))
                            .endpoint(permDef.get("endpoint"))
                            .permissionExpression(permDef.get("permissionExpression"))
                            .build();

                    permissions.add(permission);
                }

                logger.info("Loaded {} permissions from {}", permissions.size(), yamlFile);
            } else {
                logger.warn("No permissions list found in YAML file");
            }

            inputStream.close();

        } catch (Exception e) {
            logger.error("Error loading permissions from YAML: {}", e.getMessage(), e);
        }

        return permissions;
    }


    @Deprecated
    public static List<PluginPermission> loadPermissions(String yamlFile) {
        logger.warn("Using deprecated loadPermissions method without classloader");
        return loadPermissions(yamlFile, YamlPermissionLoader.class.getClassLoader());
    }
}