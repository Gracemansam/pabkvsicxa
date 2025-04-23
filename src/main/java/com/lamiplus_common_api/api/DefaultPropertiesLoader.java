package com.lamiplus_common_api.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DefaultPropertiesLoader implements PropertiesLoader {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertiesLoader.class);
    private static final String[] CONFIG_FILES = {
            "application.properties",
            "application.yml",
            "application.yaml",
            "plugin.properties",
            "plugin.yml"
    };

    @Override
    public Optional<PluginProperties> loadProperties() {
        for (String filename : CONFIG_FILES) {
            Optional<PluginProperties> properties = loadFromFile(filename);
            if (properties.isPresent()) {
                return properties;
            }
        }

        return Optional.empty();
    }

    private Optional<PluginProperties> loadFromFile(String filename) {
        if (filename.endsWith(".yml") || filename.endsWith(".yaml")) {
            return loadFromYaml(filename);
        } else {
            return loadFromProperties(filename);
        }
    }

    private Optional<PluginProperties> loadFromProperties(String filename) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                logger.debug("Properties file not found: {}", filename);
                return Optional.empty();
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            logger.info("Loaded plugin properties from {}", filename);

            return Optional.of(buildPluginProperties(properties));
        } catch (Exception e) {
            logger.warn("Error loading properties file {}: {}", filename, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<PluginProperties> loadFromYaml(String filename) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                logger.debug("YAML file not found: {}", filename);
                return Optional.empty();
            }

            // use a proper YAML parser like SnakeYAML, but this works

            Properties properties = convertYamlToProperties(inputStream);
            logger.info("Loaded plugin properties from YAML file {}", filename);

            return Optional.of(buildPluginProperties(properties));
        } catch (Exception e) {
            logger.warn("Error loading YAML file {}: {}", filename, e.getMessage());
            return Optional.empty();
        }
    }


    private Properties convertYamlToProperties(InputStream inputStream) throws IOException {

        Properties properties = new Properties();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            String currentPrefix = "";

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }


                if (line.startsWith(" ") || line.startsWith("\t")) {
                    line = line.trim();
                    int colonIndex = line.indexOf(':');
                    if (colonIndex > 0) {
                        String key = line.substring(0, colonIndex).trim();
                        String value = colonIndex < line.length() - 1 ?
                                line.substring(colonIndex + 1).trim() : "";

                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }

                        if (!value.isEmpty()) {
                            properties.setProperty(currentPrefix + "." + key, value);
                        }
                    }
                } else {
                    int colonIndex = line.indexOf(':');
                    if (colonIndex > 0) {
                        String key = line.substring(0, colonIndex).trim();
                        String value = colonIndex < line.length() - 1 ?
                                line.substring(colonIndex + 1).trim() : "";

                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }

                        if (value.isEmpty()) {
                            currentPrefix = key;
                        } else {
                            properties.setProperty(key, value);
                        }
                    }
                }
            }
        }

        return properties;
    }

    private PluginProperties buildPluginProperties(Properties properties) {
        PluginProperties pluginProps = new PluginProperties();


        pluginProps.setId(properties.getProperty("plugin.id", "patient-management"));
        pluginProps.setName(properties.getProperty("plugin.name", "Patient Management"));
        pluginProps.setDescription(properties.getProperty("plugin.description",
                "A plugin for managing patient records with full CRUD operations"));
        pluginProps.setVersion(properties.getProperty("plugin.version", "1.0.0"));
        pluginProps.setAuthor(properties.getProperty("plugin.author", "Plugin Developer"));

        String dependencies = properties.getProperty("plugin.dependencies", "");
        if (!dependencies.trim().isEmpty()) {
            pluginProps.setDependencies(Arrays.asList(dependencies.split(",")));
        }

        String roles = properties.getProperty("plugin.required.roles", "");
        if (!roles.trim().isEmpty()) {
            pluginProps.setRequiredRoles(Arrays.asList(roles.split(",")));
        }

        String actions = properties.getProperty("plugin.auditable.actions", "");
        if (!actions.trim().isEmpty()) {
            pluginProps.setAuditableActions(Arrays.asList(actions.split(",")));
        }


        String resourceTypes = properties.getProperty("plugin.auditable.resourceTypes", "");
        if (!resourceTypes.trim().isEmpty()) {
            pluginProps.setAuditableResourceTypes(Arrays.asList(resourceTypes.split(",")));
        }

        int permissionCount = Integer.parseInt(properties.getProperty("plugin.permissions.count", "0"));
        List<PermissionConfig> permissions = new ArrayList<>();

        for (int i = 0; i < permissionCount; i++) {
            String baseKey = "plugin.permission." + i + ".";
            String code = properties.getProperty(baseKey + "code", "");

            if (code.isEmpty()) {
                logger.warn("Missing permission code for index {}, skipping", Optional.of(i));
                continue;
            }

            PermissionConfig permission = new PermissionConfig();
            permission.setCode(code);
            permission.setName(properties.getProperty(baseKey + "name", code));
            permission.setActionType(properties.getProperty(baseKey + "actionType", ""));
            permission.setResourceType(properties.getProperty(baseKey + "resourceType", ""));
            permission.setDescription(properties.getProperty(baseKey + "description", ""));
            permission.setEndpoint(properties.getProperty(baseKey + "endpoint", ""));
            permission.setExpression(properties.getProperty(baseKey + "expression", ""));

            permissions.add(permission);
        }

        pluginProps.setPermissions(permissions);
        return pluginProps;
    }
}

