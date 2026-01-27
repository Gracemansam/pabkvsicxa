package com.lamiplus_common_api.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Utils {

    public static String getTenantIdFromContext() {
        log.debug("getTenantIdFromContext() called");


        try {
            Class<?> tenantContextClass = Class.forName("coreapplication.service.plugin_manager.TenantContext");
            java.lang.reflect.Method method = tenantContextClass.getMethod("getTenantId");
            String tenantId = (String) method.invoke(null);

            if (tenantId != null && !tenantId.isEmpty()) {
                log.debug("Tenant ID from core context: {}", tenantId);
                return tenantId;
            }
        } catch (ClassNotFoundException e) {
            log.debug("Core TenantContext not available - running in standalone mode");
        } catch (Exception e) {
            log.debug("Could not access core TenantContext: {}", e.getMessage());
        }


        log.debug("Checking DevTenantContext.isSet(): {}", DevTenantContext.isSet());
        if (DevTenantContext.isSet()) {
            String tenantId = DevTenantContext.getTenantId();
            log.debug("Tenant ID from dev context: {}", tenantId);
            return tenantId;
        }

        log.error("No tenant ID available - DevTenantContext.isSet() = {}", DevTenantContext.isSet());
        throw new RuntimeException("Failed to get tenant ID from context");
    }
}