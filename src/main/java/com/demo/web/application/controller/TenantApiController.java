package com.demo.web.application.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.web.application.dto.Tenant;
import com.demo.web.application.security.RequireTenantResource;
import com.demo.web.application.security.TenantAuthenticationToken;

/**
 * Sample API that uses tenant from X-Tenant-ID header.
 * Use @RequireTenantResource or @PreAuthorize to validate tenant roles.
 */
@RestController
@RequestMapping("/api")
public class TenantApiController {

    /**
     * Uses custom @RequireTenantResource: tenant must have ADMIN, EDITOR, or USER (prefix added at validation).
     */
    @GetMapping("/orders")
    @RequireTenantResource(name = "ORDERS", roles = {"ADMIN", "EDITOR", "USER"})
    public ResponseEntity<Map<String, Object>> getOrders(@AuthenticationPrincipal Tenant tenant) {
        return ResponseEntity.ok(Map.of(
            "resource", "orders",
            "tenant", tenant != null ? tenant.getCode() : "unknown",
            "message", "Orders list (tenant has required role)"
        ));
    }

    /**
     * Uses @PreAuthorize with hasAuthority: tenant must have ROLE_ADMIN.
     * Equivalent to hasRole('ADMIN') which adds ROLE_ prefix internally.
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsers(@AuthenticationPrincipal Tenant tenant) {
        return ResponseEntity.ok(Map.of(
            "resource", "users",
            "tenant", tenant != null ? tenant.getCode() : "unknown",
            "message", "Admin users (tenant has ROLE_ADMIN)"
        ));
    }

    /**
     * Uses @PreAuthorize with authority: tenant must have READ_ORDERS resource.
     */
    @GetMapping("/orders/read")
    @PreAuthorize("hasAuthority('READ_ORDERS')")
    public ResponseEntity<Map<String, Object>> readOrders(@AuthenticationPrincipal Tenant tenant) {
        return ResponseEntity.ok(Map.of(
            "resource", "orders.read",
            "tenant", tenant != null ? tenant.getCode() : "unknown",
            "message", "Read orders (tenant has READ_ORDERS)"
        ));
    }

    /**
     * Returns current tenant from security context (from X-Tenant-ID header).
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> currentTenant() {
        if (!(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication() instanceof TenantAuthenticationToken token)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Provide X-Tenant-ID header"
            ));
        }
        Tenant tenant = token.getTenant();
        return ResponseEntity.ok(Map.of(
            "code", tenant.getCode(),
            "displayName", tenant.getDisplayName(),
            "roles", tenant.getRoles().stream().map(r -> r.getName()).toList()
        ));
    }
}
