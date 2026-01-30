package com.demo.web.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import com.demo.web.application.dto.Tenant;
import com.demo.web.application.repository.TenantRepository;

/**
 * Loads all tenants with roles and resources at startup into memory.
 * Avoids DB queries on every request with X-Tenant-ID header.
 */
@Service
public class TenantCacheService implements ApplicationRunner {

    private final TenantRepository tenantRepository;

    /**
     * In-memory cache: tenant code -> Tenant (with roles and resources already loaded).
     */
    private final Map<String, Tenant> tenantByCode = new ConcurrentHashMap<>();

    public TenantCacheService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        loadCache();
    }

    /**
     * Loads all tenants (with roles and resources) from DB into cache.
     * Called at startup; can be called again to refresh after tenant/role changes.
     */
    public void loadCache() {
        tenantByCode.clear();
        tenantRepository.findAll().forEach(tenant -> {
            // Tenant has EAGER roles, Role has EAGER resources - all loaded in one go
            tenantByCode.put(tenant.getCode().toLowerCase().trim(), tenant);
        });
        System.out.println("=== Tenant cache loaded: " + tenantByCode.size() + " tenants ===");
    }

    /**
     * Returns tenant by code from cache (no DB hit).
     */
    public Optional<Tenant> getTenantByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tenantByCode.get(code.toLowerCase().trim()));
    }

    /**
     * Call after tenant/role/resource data changes to reload from DB.
     */
    public void refreshCache() {
        loadCache();
    }
}
