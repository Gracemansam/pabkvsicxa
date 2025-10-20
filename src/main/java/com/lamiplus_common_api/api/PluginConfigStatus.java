package com.lamiplus_common_api.api;


public enum PluginConfigStatus {

    INHERITED,


    ENABLED,


    DISABLED,

    CUSTOMIZED,


    PENDING;


    public boolean isActive() {
        return this == INHERITED || this == ENABLED || this == CUSTOMIZED;
    }
}