package com.lamiplus_common_api.api;

/**
 * Plugin type classification
 */
public enum PluginType {
    /**
     * Base/core plugins that auto-load on startup
     * Example: Authentication, Audit Logging
     */
    BASE,


    SERVICE,


    UTILITY,


    INTEGRATION,

    /**
     * UI enhancement plugins
     * Example: Custom Dashboard Widgets
     */
    UI_COMPONENT,

    /**
     * Analytics and reporting plugins
     */
    ANALYTICS,

    /**
     * Security and compliance plugins
     */
    SECURITY
}