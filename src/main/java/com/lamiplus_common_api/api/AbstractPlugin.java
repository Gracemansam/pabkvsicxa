package com.lamiplus_common_api.api;

import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

public abstract class AbstractPlugin implements Plugin {

    private PluginInfo info;
    private Logger logger;
    private ApplicationContext pluginContext;

    @Override
    public void initialize(PluginInfo info) {
        this.info = info;
        this.logger = new PluginLogger(info.getPluginId());
        logger.info("Initializing plugin: {} ({})", info.getName(), info.getPluginId());
    }

    @Override
    public String getPluginId() {
        return info != null ? info.getPluginId() : "unknown";
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = new PluginLogger(getPluginId());
        }
        return logger;
    }

    protected PluginInfo getPluginInfo() {
        return info;
    }

    public void setPluginContext(ApplicationContext pluginContext) {
        this.pluginContext = pluginContext;
        getLogger().debug("Plugin context injected for: {}", getPluginId());
    }


    public ApplicationContext getPluginContext() {
        return pluginContext;
    }


    @Override
    public <T> Optional<T> getService(Class<T> serviceClass) {
        if (pluginContext == null) {
            getLogger().warn("Plugin context not available for service lookup: {}", serviceClass.getSimpleName());
            return Optional.empty();
        }

        try {
            T service = pluginContext.getBean(serviceClass);
            getLogger().debug("Found service {} in plugin {}", serviceClass.getSimpleName(), getPluginId());
            return Optional.of(service);
        } catch (Exception e) {
            getLogger().debug("Service {} not found in plugin {}: {}",
                    serviceClass.getSimpleName(), getPluginId(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> getService(String serviceName, Class<T> serviceClass) {
        if (pluginContext == null) {
            getLogger().warn("Plugin context not available for service lookup: {}", serviceName);
            return Optional.empty();
        }

        try {
            T service = pluginContext.getBean(serviceName, serviceClass);
            getLogger().debug("Found service {} in plugin {}", serviceName, getPluginId());
            return Optional.of(service);
        } catch (Exception e) {
            getLogger().debug("Service {} not found in plugin {}: {}",
                    serviceName, getPluginId(), e.getMessage());
            return Optional.empty();
        }
    }
}