package com.lamiplus_common_api.common;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@Profile("dev")

@RequiredArgsConstructor
public class DevTenantInterceptor implements HandlerInterceptor {

    @Value("${plugin.standalone.tenant-id:demo-hospital}")
    private String devTenantId;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("DevTenantInterceptor: Setting tenant to {}", devTenantId);
        DevTenantContext.setTenantId(devTenantId);
        log.debug("DevTenantContext.getTenantId(): {}", DevTenantContext.getTenantId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        log.debug("DevTenantInterceptor: Clearing tenant context");
        DevTenantContext.clear();
    }
}