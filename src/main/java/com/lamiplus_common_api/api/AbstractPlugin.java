package com.lamiplus_common_api.api;



import org.slf4j.Logger;

public abstract class AbstractPlugin implements Plugin {

    private PluginInfo info;
    private Logger logger;

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
}
