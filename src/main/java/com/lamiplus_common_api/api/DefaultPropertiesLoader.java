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
            "plugin.yml",
            "plugin.yaml"
    };

    @Override
    public Optional<PluginProperties> loadProperties() {
        logger.debug("Starting to load plugin properties...");

        ClassLoader classLoader = getClass().getClassLoader();
        logger.debug("Classpath resources access test result: {}",
                (classLoader.getResource("") != null ? "accessible" : "not accessible"));

        for (String filename : CONFIG_FILES) {
            logger.debug("Attempting to load properties from: {}", filename);

            try (InputStream testStream = classLoader.getResourceAsStream(filename)) {
                if (testStream != null) {
                    logger.debug("Found resource file: {}", filename);
                } else {
                    logger.debug("Resource not found: {}", filename);
                }
            } catch (IOException e) {
                logger.warn("Error checking for resource: {}", filename, e);
            }

            Optional<PluginProperties> properties = loadFromFile(filename);
            if (properties.isPresent()) {

                PluginProperties props = properties.get();
                logger.debug("Loaded properties: id={}, name={}, version={}",
                        props.getId(), props.getName(), props.getVersion());

                return properties;
            }
        }

        logger.warn("No properties files were found or successfully loaded");
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


            logger.debug("Properties loaded from {}: {}", filename,
                    properties.stringPropertyNames().stream()
                            .filter(key -> key.startsWith("plugin."))
                            .count() + " plugin-related properties");

            return Optional.of(buildPluginProperties(properties));
        } catch (Exception e) {
            logger.warn("Error loading properties file {}: {}", filename, e.getMessage());
            logger.debug("Stack trace for properties loading error:", e);
            return Optional.empty();
        }
    }

    private Optional<PluginProperties> loadFromYaml(String filename) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                logger.debug("YAML file not found: {}", filename);
                return Optional.empty();
            }


            Properties properties = convertYamlToProperties(inputStream);
            logger.info("Loaded plugin properties from YAML file {}", filename);


            logger.debug("Properties loaded from YAML {}: {}", filename,
                    properties.stringPropertyNames().stream()
                            .filter(key -> key.startsWith("plugin"))
                            .collect(java.util.stream.Collectors.toList()));

            return Optional.of(buildPluginProperties(properties));
        } catch (Exception e) {
            logger.warn("Error loading YAML file {}: {}", filename, e.getMessage());
            logger.debug("Stack trace for YAML loading error:", e);
            return Optional.empty();
        }
    }

    private Properties convertYamlToProperties(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        Map<Integer, String> prefixStack = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }


                int indent = 0;
                while (indent < line.length() && (line.charAt(indent) == ' ' || line.charAt(indent) == '\t')) {
                    indent++;
                }
                indent = indent / 2;

                line = line.trim();

                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = colonIndex < line.length() - 1 ?
                            line.substring(colonIndex + 1).trim() : "";

                    // Clean value (remove quotes)
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }


                    StringBuilder fullKey = new StringBuilder();

                    for (int i = indent + 1; i < 10; i++) {
                        prefixStack.remove(i);
                    }


                    for (int i = 0; i < indent; i++) {
                        if (prefixStack.containsKey(i)) {
                            if (fullKey.length() > 0) {
                                fullKey.append(".");
                            }
                            fullKey.append(prefixStack.get(i));
                        }
                    }

                    if (fullKey.length() > 0) {
                        fullKey.append(".");
                    }
                    fullKey.append(key);

                    String finalKey = fullKey.toString();

                    if (value.isEmpty()) {
                        prefixStack.put(indent, key);
                        logger.trace("Added prefix at level {}: {}", indent, key);
                    } else {
                        // It's a key-value pair, store it
                        properties.setProperty(finalKey, value);
                        logger.trace("Added property: {}={}", finalKey, value);
                    }
                }
            }
        }

        return properties;
    }

    private PluginProperties buildPluginProperties(Properties properties) {
        PluginProperties pluginProps = new PluginProperties();


        if (logger.isDebugEnabled()) {
            List<String> pluginKeys = new ArrayList<>();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("plugin")) {
                    pluginKeys.add(key + "=" + properties.getProperty(key));
                }
            }
            logger.debug("Available plugin properties before building: {}", pluginKeys);
        }

        pluginProps.setId(getProperty(properties, "plugin.id", "plugin.id", "patient-management"));
        pluginProps.setName(getProperty(properties, "plugin.name", "plugin.name", "Patient Management"));
        pluginProps.setDescription(getProperty(properties, "plugin.description", "plugin.description",
                "A plugin for managing patient records with full CRUD operations"));
        pluginProps.setVersion(getProperty(properties, "plugin.version", "plugin.version", "1.0.0"));
        pluginProps.setAuthor(getProperty(properties, "plugin.author", "plugin.author", "Plugin Developer"));

        // Log the values that were actually used
        logger.debug("Plugin properties set - id: {}, name: {}, version: {}",
                pluginProps.getId(), pluginProps.getName(), pluginProps.getVersion());

        String dependencies = getProperty(properties, "plugin.dependencies", "plugin.dependencies", "");
        if (!dependencies.trim().isEmpty()) {
            pluginProps.setDependencies(Arrays.asList(dependencies.split(",")));
        }

        String roles = getProperty(properties, "plugin.required.roles", "plugin.required.roles", "");
        if (!roles.trim().isEmpty()) {
            pluginProps.setRequiredRoles(Arrays.asList(roles.split(",")));
        }

        String actions = getProperty(properties, "plugin.auditable.actions", "plugin.auditable.actions", "");
        if (!actions.trim().isEmpty()) {
            pluginProps.setAuditableActions(Arrays.asList(actions.split(",")));
        }

        String resourceTypes = getProperty(properties, "plugin.auditable.resourceTypes",
                "plugin.auditable.resourceTypes", "");
        if (!resourceTypes.trim().isEmpty()) {
            pluginProps.setAuditableResourceTypes(Arrays.asList(resourceTypes.split(",")));
        }

        int permissionCount = Integer.parseInt(
                getProperty(properties, "plugin.permissions.count", "plugin.permissions.count", "0")
        );
        List<PermissionConfig> permissions = new ArrayList<>();

        for (int i = 0; i < permissionCount; i++) {
            String baseKeyDot = "plugin.permission." + i + ".";
            String baseKey = "plugin.permission." + i + ".";

            String code = getProperty(properties, baseKeyDot + "code", baseKey + "code", "");

            if (code.isEmpty()) {
                logger.warn("Missing permission code for index {}, skipping", i);
                continue;
            }

            PermissionConfig permission = new PermissionConfig();
            permission.setCode(code);
            permission.setName(getProperty(properties, baseKeyDot + "name", baseKey + "name", code));
            permission.setActionType(getProperty(properties, baseKeyDot + "actionType", baseKey + "actionType", ""));
            permission.setResourceType(getProperty(properties, baseKeyDot + "resourceType", baseKey + "resourceType", ""));
            permission.setDescription(getProperty(properties, baseKeyDot + "description", baseKey + "description", ""));
            permission.setEndpoint(getProperty(properties, baseKeyDot + "endpoint", baseKey + "endpoint", ""));
            permission.setExpression(getProperty(properties, baseKeyDot + "expression", baseKey + "expression", ""));

            permissions.add(permission);
        }

        pluginProps.setPermissions(permissions);
        return pluginProps;
    }


    private String getProperty(Properties properties, String keyWithDot, String keyWithoutDot, String defaultValue) {
        String value = properties.getProperty(keyWithDot);

        // If not found with dot, try without dot
        if (value == null) {
            value = properties.getProperty(keyWithoutDot);
        }

        // If still not found, use default
        if (value == null) {
            return defaultValue;
        }

        return value;
    }
}