package com.demo.web.application.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.demo.web.application.dto.Tenant;

/**
 * Authentication token holding the current tenant (from X-Tenant-ID header).
 * Used so @PreAuthorize("hasAuthority('ROLE_ORDERS')") evaluates against tenant's roles.
 */
public class TenantAuthenticationToken extends AbstractAuthenticationToken {

    private final Tenant tenant;

    public TenantAuthenticationToken(Tenant tenant) {
        super(tenant.getAuthorities());
        this.tenant = tenant;
        setAuthenticated(true);
    }

    public TenantAuthenticationToken(Tenant tenant, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.tenant = tenant;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return tenant;
    }

    public Tenant getTenant() {
        return tenant;
    }
}
