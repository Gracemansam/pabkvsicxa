package com.lamiplus_common_api.api;


public interface PluginContext {


    String getTenantId();

    Long getOrgUnitId();


    String getUserId();


    String getHierarchyPath();


    boolean isTenantAdmin();


    <T> T getConfiguration(Class<T> configClass);


    Object getConfigurationValue(String key);


    void setAttribute(String key, Object value);

    Object getAttribute(String key);
}
