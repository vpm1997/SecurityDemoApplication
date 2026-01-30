package com.demo.web.application.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.demo.web.application.dto.Resource;
import com.demo.web.application.dto.Role;
import com.demo.web.application.dto.Tenant;
import com.demo.web.application.repository.ResourceRepository;
import com.demo.web.application.repository.RoleRepository;
import com.demo.web.application.repository.TenantRepository;
import com.demo.web.application.service.TenantCacheService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final TenantRepository tenantRepository;
    private final TenantCacheService tenantCacheService;

    public DataInitializer(RoleRepository roleRepository, ResourceRepository resourceRepository,
                           TenantRepository tenantRepository, TenantCacheService tenantCacheService) {
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.tenantRepository = tenantRepository;
        this.tenantCacheService = tenantCacheService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Migrate existing roles: store without ROLE_ prefix in DB
        roleRepository.findByName("ROLE_USER").ifPresent(r -> { r.setName("USER"); roleRepository.save(r); });
        roleRepository.findByName("ROLE_ADMIN").ifPresent(r -> { r.setName("ADMIN"); roleRepository.save(r); });
        roleRepository.findByName("ROLE_EDITOR").ifPresent(r -> { r.setName("EDITOR"); roleRepository.save(r); });

        // Create resources (permissions) if they don't exist
        Resource readOrders = getOrCreateResource("READ_ORDERS");
        Resource writeOrders = getOrCreateResource("WRITE_ORDERS");
        Resource deleteOrders = getOrCreateResource("DELETE_ORDERS");
        
        Resource readUsers = getOrCreateResource("READ_USERS");
        Resource writeUsers = getOrCreateResource("WRITE_USERS");
        Resource deleteUsers = getOrCreateResource("DELETE_USERS");
        
        Resource readDesign = getOrCreateResource("READ_DESIGN");
        Resource writeDesign = getOrCreateResource("WRITE_DESIGN");

        // Create USER role with basic permissions (stored without ROLE_ prefix)
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role("USER");
            role.addResource(readOrders);
            role.addResource(writeOrders);
            role.addResource(readDesign);
            role.addResource(writeDesign);
            return roleRepository.save(role);
        });

        // Create ADMIN role with all permissions
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role role = new Role("ADMIN");
            role.addResource(readOrders);
            role.addResource(writeOrders);
            role.addResource(deleteOrders);
            role.addResource(readUsers);
            role.addResource(writeUsers);
            role.addResource(deleteUsers);
            role.addResource(readDesign);
            role.addResource(writeDesign);
            return roleRepository.save(role);
        });

        // Create EDITOR role with limited permissions
        Role editorRole = roleRepository.findByName("EDITOR").orElseGet(() -> {
            Role role = new Role("EDITOR");
            role.addResource(readOrders);
            role.addResource(writeOrders);
            role.addResource(readDesign);
            role.addResource(writeDesign);
            role.addResource(readUsers);
            return roleRepository.save(role);
        });

        // Create tenants with roles (for API X-Tenant-ID header)
        Tenant tenantAcme = tenantRepository.findByCode("acme").orElseGet(() -> {
            Tenant t = new Tenant("acme", "Acme Corp");
            t.addRole(adminRole);
            return tenantRepository.save(t);
        });
        Tenant tenantBeta = tenantRepository.findByCode("beta").orElseGet(() -> {
            Tenant t = new Tenant("beta", "Beta Inc");
            t.addRole(editorRole);
            return tenantRepository.save(t);
        });
        Tenant tenantGamma = tenantRepository.findByCode("gamma").orElseGet(() -> {
            Tenant t = new Tenant("gamma", "Gamma LLC");
            t.addRole(userRole);
            return tenantRepository.save(t);
        });

        Tenant tenantDemo = tenantRepository.findByCode("demo").orElseGet(() -> {
            Tenant t = new Tenant("demo", "Demo Tenant");
            t.addRole(userRole);
            return tenantRepository.save(t);
        });

        System.out.println("=== Roles and Resources initialized ===");
        System.out.println("USER resources: " + userRole.getResources());
        System.out.println("ADMIN resources: " + adminRole.getResources());
        System.out.println("EDITOR resources: " + editorRole.getResources());
        System.out.println("=== Tenants (use X-Tenant-ID header) ===");
        System.out.println("acme -> ADMIN, beta -> EDITOR, gamma -> USER, demo -> USER");

        // Load tenant cache so no DB hit on every API request
        tenantCacheService.refreshCache();
    }

    private Resource getOrCreateResource(String name) {
        return resourceRepository.findByName(name)
                .orElseGet(() -> resourceRepository.save(new Resource(name)));
    }
}
