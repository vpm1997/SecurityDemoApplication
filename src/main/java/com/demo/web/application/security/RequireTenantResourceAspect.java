package com.demo.web.application.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Enforces @RequireTenantResource: validates that the current tenant (from X-Tenant-ID header)
 * has at least one of the required roles.
 */
@Aspect
@Component
public class RequireTenantResourceAspect {

    @Around("@annotation(requireTenantResource)")
    public Object checkTenantResource(ProceedingJoinPoint joinPoint, RequireTenantResource requireTenantResource)
            throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new TenantAccessDeniedException("No tenant context. Provide X-Tenant-ID header.");
        }

        if (!(auth instanceof TenantAuthenticationToken)) {
            throw new TenantAccessDeniedException("Tenant context required. Provide X-Tenant-ID header.");
        }

        // Normalize: annotation uses DB role names (ADMIN, USER); add ROLE_ prefix for comparison
        Set<String> requiredAuthorities = Arrays.stream(requireTenantResource.roles())
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toSet());

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        Set<String> tenantAuthorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean hasRole = requiredAuthorities.stream().anyMatch(tenantAuthorities::contains);

        if (!hasRole) {
            String resourceName = requireTenantResource.name().isEmpty()
                    ? joinPoint.getSignature().toShortString()
                    : requireTenantResource.name();
            throw new TenantAccessDeniedException(
                    "Tenant does not have required role for resource '" + resourceName + "'. " +
                    "Required one of: " + requiredAuthorities + ", tenant has: " + tenantAuthorities);
        }

        return joinPoint.proceed();
    }
}
