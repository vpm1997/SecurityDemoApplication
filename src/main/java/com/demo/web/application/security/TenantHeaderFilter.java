package com.demo.web.application.security;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.web.application.service.TenantCacheService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Extracts tenant from X-Tenant-ID header and sets tenant's authorities in SecurityContext.
 * Uses in-memory cache (loaded at startup) - no DB query per request.
 */
@Service
public class TenantHeaderFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private final TenantCacheService tenantCacheService;

    public TenantHeaderFilter(TenantCacheService tenantCacheService) {
        this.tenantCacheService = tenantCacheService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantCode = request.getHeader(TENANT_HEADER);

        if (tenantCode != null && !tenantCode.isBlank()) {
            tenantCacheService.getTenantByCode(tenantCode.trim())
                .ifPresent(tenant -> {
                    TenantAuthenticationToken token = new TenantAuthenticationToken(tenant);
                    SecurityContextHolder.getContext().setAuthentication(token);
                });
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Optional: clear tenant auth after request if you want API-only tenant context
            // SecurityContextHolder.clearContext();
        }
    }
}
