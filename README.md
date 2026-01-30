# Security Demo Application

Spring Boot demo with **database-backed roles/resources** and **multi-tenant API authorization** using the `X-Tenant-ID` header.

## Tech Stack

- **Java 21** · **Spring Boot 3.2** · **Spring Security 6** · **Spring Data JPA**
- **MySQL** for persistence
- **Thymeleaf** for web UI (login, registration, orders, design)

## Features

- **Roles and resources in DB** – Roles (e.g. `USER`, `ADMIN`, `EDITOR`) and fine-grained resources (e.g. `READ_ORDERS`, `WRITE_USERS`) stored in the database; `ROLE_` prefix added in code for Spring Security.
- **Multi-tenant API** – Tenant identity from `X-Tenant-ID` header; tenant’s roles and resources used for authorization.
- **Tenant filter** – `TenantHeaderFilter` resolves tenant from header, loads from in-memory cache, and sets `TenantAuthenticationToken` in the security context.
- **Authorization options** – Custom `@RequireTenantResource(roles = {"ADMIN", "EDITOR"})` (roles without prefix; prefix added at validation) and standard `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` / `hasAuthority('READ_ORDERS')`.

## Prerequisites

- Java 21
- Maven
- MySQL (e.g. local on port 3306)

## Database

Create the database and configure credentials:

```sql
CREATE DATABASE IF NOT EXISTS securityDemo;
```

Default config in `application.properties`:

- URL: `jdbc:mysql://localhost:3306/securityDemo`
- Username: `root`
- Password: `root`

Override with env vars: `MYSQL_HOST`, or set `spring.datasource.*` in a profile.

## Run

```bash
./mvnw spring-boot:run
```

App runs at **http://localhost:8080**.

- **Web:** `/`, `/login`, `/register`, `/orders`, `/design` (form login; user roles from DB).
- **API:** `/api/**` – use `X-Tenant-ID` header for tenant context.

## Sample Tenants (seeded at startup)

| X-Tenant-ID | Role   | Notes                          |
|-------------|--------|---------------------------------|
| `acme`      | ADMIN  | Full access, incl. `/api/admin/users` |
| `beta`      | EDITOR | Orders + read users            |
| `gamma`     | USER   | Orders + design                |
| `demo`      | USER   | Same as gamma                  |

## API Examples

All `/api/**` requests should send the tenant header. Missing or invalid tenant returns 403.

**Current tenant (requires header):**
```bash
curl -s -H "X-Tenant-ID: acme" http://localhost:8080/api/me
```

**Orders (ADMIN, EDITOR, or USER):**
```bash
curl -s -H "X-Tenant-ID: acme" http://localhost:8080/api/orders
curl -s -H "X-Tenant-ID: gamma" http://localhost:8080/api/orders
```

**Admin users (ADMIN only):**
```bash
curl -s -H "X-Tenant-ID: acme" http://localhost:8080/api/admin/users   # 200
curl -s -H "X-Tenant-ID: beta" http://localhost:8080/api/admin/users   # 403
```

**Read orders (authority READ_ORDERS):**
```bash
curl -s -H "X-Tenant-ID: acme" http://localhost:8080/api/orders/read
```

## Project Structure (main pieces)

- **Security**
  - `TenantHeaderFilter` – reads `X-Tenant-ID`, sets tenant auth from cache.
  - `TenantAuthenticationToken` – holds `Tenant` and its authorities.
  - `RequireTenantResource` + `RequireTenantResourceAspect` – enforce required roles (DB-style names; prefix added at validation).
  - `SecurityConfig` – filter chain, form login, method security.

- **Domain / persistence**
  - `User`, `Tenant`, `Role`, `Resource` – JPA entities; roles/resources in DB without `ROLE_` prefix.
  - `TenantCacheService` – in-memory tenant cache (by code) loaded at startup.

- **API**
  - `TenantApiController` – sample endpoints using `@RequireTenantResource` and `@PreAuthorize`.

- **Config**
  - `DataInitializer` – seeds roles (USER, ADMIN, EDITOR), resources, and sample tenants; migrates old `ROLE_*` names to short form on startup.

## License

Demo / educational use.
