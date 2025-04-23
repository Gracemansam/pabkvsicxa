package com.lamiplus_common_api.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})

@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action() default "";
    String resourceType() default "";
    String resourceId() default "";
    boolean excludeFromAudit() default false;
}