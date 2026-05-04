# Local Multitenancy Setup

## 1. Copy Tenant Configuration

Copy the tenant configuration file from `/docker` into your application resources:

```bash
cp docker/tenants.yaml src/main/resources/tenants.yaml
```

---

## 2. Configure Application

Update `application.yaml` (multitenant profile):

```yaml
"%multitenant":
  config:
    locations: /tenants.yaml
```

---

## 3. Configure Tenant Datasources

Edit `tenants.yaml` and replace the datasource section with:

```yaml
datasource:
  tenant_1:
    db-kind: postgresql
    username: damap
    password: pw4damap
    jdbc:
      url: jdbc:postgresql://localhost:8088/tenant_1

  tenant_2:
    db-kind: postgresql
    username: damap
    password: pw4damap
    jdbc:
      url: jdbc:postgresql://localhost:8088/tenant_2
```

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

## 6. Test Users

```
tenant_1 user:
  username: tenant1-user
  password: password

tenant_2 user:
  username: tenant2-user
  password: password

tenant_1 admin:
  username: tenant_1_admin
  password: password

tenant_2 admin:
  username: tenant_2_admin
  password: password
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