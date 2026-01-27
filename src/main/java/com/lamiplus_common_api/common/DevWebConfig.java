package com.lamiplus_common_api.common;



import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevWebConfig implements WebMvcConfigurer {

    private final DevTenantInterceptor devTenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(devTenantInterceptor)
                .addPathPatterns("/**");
    }
}