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

    public static UserInfo getCurrentUser() {
        log.debug("getCurrentUser() called");

        try {
            Class<?> securityContextHolderClass = Class.forName(
                    "org.springframework.security.core.context.SecurityContextHolder"
            );
            java.lang.reflect.Method getContext = securityContextHolderClass.getMethod("getContext");
            Object securityContext = getContext.invoke(null);

            java.lang.reflect.Method getAuthentication = securityContext.getClass()
                    .getMethod("getAuthentication");
            Object authentication = getAuthentication.invoke(securityContext);

            if (authentication == null) {
                log.debug("No authentication found in SecurityContext");
                return null;
            }

            java.lang.reflect.Method isAuthenticated = authentication.getClass()
                    .getMethod("isAuthenticated");
            boolean authenticated = (boolean) isAuthenticated.invoke(authentication);

            if (!authenticated) {
                log.debug("User is not authenticated");
                return null;
            }

            java.lang.reflect.Method getPrincipal = authentication.getClass()
                    .getMethod("getPrincipal");
            Object principal = getPrincipal.invoke(authentication);

            if (principal == null || "anonymousUser".equals(principal)) {
                log.debug("Principal is anonymous or null");
                return null;
            }

            try {
                Class<?> customUserDetailsClass = Class.forName(
                        "coreapplication.security.CustomUserDetails"
                );

                if (customUserDetailsClass.isInstance(principal)) {
                    java.lang.reflect.Method getUser = customUserDetailsClass.getMethod("getUser");
                    Object user = getUser.invoke(principal);

                    Class<?> userClass = user.getClass();

                    String email = (String) userClass.getMethod("getEmail").invoke(user);
                    String fullName = (String) userClass.getMethod("getFullName").invoke(user);
                    String userId = (String) userClass.getMethod("getId").invoke(user);
                    String tenantId = (String) userClass.getMethod("getTenantId").invoke(user);

                    log.debug("Current user from CustomUserDetails: {} (tenant: {})", email, tenantId);

                    return new UserInfo(userId, email, fullName, tenantId);
                }
            } catch (ClassNotFoundException e) {
                log.debug("CustomUserDetails not available - running in standalone mode");
            }

            if (principal instanceof String username) {
                log.debug("Current user from String principal: {}", username);
                return new UserInfo(null, username, username, getTenantIdSafe());
            }

            java.lang.reflect.Method getName = authentication.getClass().getMethod("getName");
            String name = (String) getName.invoke(authentication);
            log.debug("Current user from authentication.getName(): {}", name);
            return new UserInfo(null, name, name, getTenantIdSafe());

        } catch (Exception e) {
            log.error("Could not get current user from SecurityContext: {}", e.getMessage(), e);
            return null;
        }
    }

    public static String getCurrentUserEmail() {
        UserInfo user = getCurrentUser();
        return user != null ? user.email() : "system";
    }

    public static String getCurrentUserFullName() {
        UserInfo user = getCurrentUser();
        return user != null ? user.fullName() : null;
    }

    private static String getTenantIdSafe() {
        try {
            return getTenantIdFromContext();
        } catch (Exception e) {
            return null;
        }
    }

    public record UserInfo(
            String userId,
            String email,
            String fullName,
            String tenantId
    ) {}
}