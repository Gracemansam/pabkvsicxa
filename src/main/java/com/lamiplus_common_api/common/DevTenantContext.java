package com.lamiplus_common_api.common;



import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DevTenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
        log.debug("Dev mode: Set tenant context to: {}", tenantId);
    }

    public static String getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }

    public static boolean isSet() {
        return currentTenant.get() != null;
    }
}
