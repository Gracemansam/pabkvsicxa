package com.lamiplus_common_api.api;

public enum PluginStatus {

    INSTALLED,

    LOADING,


    STOPPED,


    ACTIVE,


    RUNNING,


    ERROR,


    MISSING_DEPENDENCIES,


    UNLOADING,


    DISABLED,


    UNHEALTHY;


    public boolean isRunning() {
        return this == ACTIVE || this == RUNNING;
    }


    public boolean isError() {
        return this == ERROR || this == UNHEALTHY || this == MISSING_DEPENDENCIES;
    }


    public boolean canStart() {
        return this == INSTALLED || this == STOPPED || this == DISABLED;
    }


    public boolean canStop() {
        return this == ACTIVE || this == RUNNING;
    }
}