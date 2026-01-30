package com.demo.web.application.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares which tenant roles are allowed to access an API.
 * Use on controller methods; tenant is taken from X-Tenant-ID header.
 * Tenant must have at least one of the listed roles.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTenantResource {

    /**
     * Resource/resource name (e.g. "ORDERS", "USERS") for documentation/logging.
     */
    String name() default "";

    /**
     * Roles that are allowed to access this API. Tenant must have at least one.
     */
    String[] roles();
}
