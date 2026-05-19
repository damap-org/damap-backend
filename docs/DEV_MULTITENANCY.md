# Local Multitenancy Setup

## 1. Copy Tenant Configuration

Copy the sample tenant configuration file from `/docker` into your application resources:

```bash
cp docker/tenants.yaml src/main/resources/tenants.yaml
```

---

## 2. Configure Application

Update `application.yaml` (multitenant profile) and set location to the new location of your `tenants.yaml` config file:

```yaml
"%multitenant":
  config:
    locations: /tenants.yaml
```

Take care not to commit this change after testing or use the CLI flag `Dquarkus.config.locations=classpath:tenants.yaml`.

---

## 3. Configure Tenant Datasources

Edit `tenants.yaml` and replace the datasource section with:

```yaml
datasource:
  tenant_1:
    jdbc:
      url: jdbc:postgresql://localhost:8088/tenant_1
      driver: ${quarkus.datasource.jdbc.driver}
    db-kind: ${quarkus.datasource.db-kind}
    username: ${quarkus.datasource.username}
    password: ${quarkus.datasource.password}
  tenant_2:
    jdbc:
      url: jdbc:postgresql://localhost:8088/tenant_2
      driver: ${quarkus.datasource.jdbc.driver}
    db-kind: ${quarkus.datasource.db-kind}
    username: ${quarkus.datasource.username}
    password: ${quarkus.datasource.password}
```

You can also change tenant-specific configs by playing around with the `tenant-configs:` section. This can be useful if
you e.g. want to configure pure services for one tenant.

---

## 4. Create Tenant Databases

Run inside the Postgres container:

```bash
psql -U damap -d postgres -c "CREATE DATABASE tenant_1 OWNER damap;"
psql -U damap -d postgres -c "CREATE DATABASE tenant_2 OWNER damap;"
```

---

## 5. Start the Application

Start Docker (DB + Keycloak), then run:

```bash
mvn quarkus:dev -Dquarkus.profile=dev,multitenant
```

---

## 6. Test Users in Keycloak

```
tenant_1 user:
  username: tenant1_user
  password: tenant1_user

tenant_2 user:
  username: tenant2_user
  password: tenant2_user

tenant_1 admin:
  username: tenant1_admin
  password: tenant1_admin

tenant_2 admin:
  username: tenant2_admin
  password: tenant2_admin
```

---

## Notes

- Default users (`user`, `admin`) are **not associated with any tenant** and will not work.
- Tenant resolution depends on the **affiliation claim** in the JWT.
- If tenants are not detected, verify:
    - `tenants.yaml` exists in `src/main/resources`
    - `config.locations` is set to `/tenants.yaml`
- You can create or modify test users by editing `/docker/sample-damap-realm-export.json`.  
  After changes, restart Keycloak (or re-import the realm) to apply them.
