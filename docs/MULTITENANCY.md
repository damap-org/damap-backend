# Multitenancy

One DAMAP instance can be configured to support multiple separate tenants (usually universities), which is most useful
in Kubernetes-based deployments. This works by assigning each new tenant their own separate database, and by checking
authentication tokens to determine which organisation a user belongs to. Depending on affiliation, the correct database
is used - this way user data stays completely separate between tenants.

## Implementation in Quarkus

Quarkus offers [native support](https://quarkus.io/guides/hibernate-orm#multitenancy) for multitenancy.
Multiple approaches are possible, this project uses the [database approach](https://quarkus.io/guides/hibernate-orm#database-approach).
This approach requires a list of tenants at build time and a unique, seperated datasource per tenant.

DAMAP differentiates between tenants by reading the tenants affiliation from the authentication token.
This means that the OIDC server needs to provide this information in the correct claim.
The claim can be configured with `DAMAP_AUTH_AFFILIATIONS_CLAIM`, e.g. if your identity provider puts this information
under `affiliation`, then set the environment variable to `affiliation`.
Affiliations are transformed internally by replacing every non-alphanumeric character with an `_`.
`university.ac.at` will therefore become `university_ac_at`.
Mapping an affiliation to the correct database takes place in the [CustomTenantResolver](../src/main/java/org/damap/base/hibernate/CustomTenantResolver.java).

> [!IMPORTANT]
> Every tenant database needs to exactly have the same name as the transformed affiliation, otherwise Quarkus cant map
> tenant and database.

Since DAMAP needs to run as a single and multitenant application

Multitenancy is activated  by setting the `QUARKUS_PROFILE` environment variable in the .env file to `multitenant` in order to
activate the multitenant quarkus profile.
If you need to activate multiple profiles, pass a value like this: `multitenant,xyz`.
The 'multitenant' profile deactivates the default unnamed database and reactives it under the name 'bootstrap'.
As DAMAP also needs to run as a normal application, the unnamed data source is configured as the default.
However, multitenancy requires a named data source, so the renaming is necessary.
The profile activates multitenancy mode like this:

```
    hibernate-orm:
      datasource: bootstrap
      multitenant: DATABASE
```

The profile also specifies an external config file to be loaded.

```
    config:
      locations: /opt/damap/tenants.yaml
```

It expects a config file like this:

```
damap:
  tenants:
    tenant-list: ['tenant_1', 'tenant_2']
    # these configs exactly mirror the damap.tenant-aware configs in application.yaml
    tenant-configs:
      tenant_1:
        title: Tenant 1 DAMAP Tool
        fields:
          ethical-report-enabled: false
        project-service: default
        elsevier-pure-description-classification: /dk/atira/pure/projectdescription
        elsevier-pure-contributor-role-classifications:
          - pure-role-uri: /dk/atira/pure/member
            contributor-role: PROJECT_MEMBER
        elsevier-pure-project-lead-role-classification: /dk/atira/pure/projectlead
        elsevier-pure-backend: http
        elsevier-pure-endpoint-url: "https://your-tenant1-pure-instance.elsevierpure.com/ws/api/"
        elsevier-pure-api-key: "your API key here"
        elsevier-pure-projects-file: "file:///path/to/projects.json"
        elsevier-pure-persons-file: "file:///path/to/persons.json"
        person-services:
          - display-text: 'ORCID'
            query-value: 'ORCID'
            class-name: 'org.damap.base.integration.orcid.ORCIDPersonServiceImpl'
          - display-text: 'University'
            query-value: 'UNIVERSITY'
            class-name: 'org.damap.base.integration.mock.MockUniversityPersonServiceImpl'
      tenant_2:
        title: Tenant 2 DAMAP Tool
        fields:
          ethical-report-enabled: false
        project-service: default
        elsevier-pure-description-classification: /dk/atira/pure/projectdescription
        elsevier-pure-contributor-role-classifications:
          - pure-role-uri: /dk/atira/pure/member
            contributor-role: PROJECT_MEMBER
        elsevier-pure-project-lead-role-classification: /dk/atira/pure/projectlead
        elsevier-pure-backend: http
        elsevier-pure-endpoint-url: "https://your-tenant2-pure-instance.elsevierpure.com/ws/api"
        elsevier-pure-api-key: "your API key here"
        elsevier-pure-projects-file: "file:///path/to/projects.json"
        elsevier-pure-persons-file: "file:///path/to/persons.json"
        person-services:
          - display-text: 'ORCID'
            query-value: 'ORCID'
            class-name: 'org.damap.base.integration.orcid.ORCIDPersonServiceImpl'
          - display-text: 'University'
            query-value: 'UNIVERSITY'
            class-name: 'org.damap.base.integration.mock.MockUniversityPersonServiceImpl'

"%multitenant":
  quarkus:
    datasource:
      tenant_1:
        jdbc:
          url: jdbc:postgresql://damap-db:5432/tenant_1
          driver: ${quarkus.datasource.jdbc.driver}
        db-kind: ${quarkus.datasource.db-kind}
        username: ${quarkus.datasource.username}
        password: ${quarkus.datasource.password}
      tenant_2:
        jdbc:
          url: jdbc:postgresql://damap-db:5432/tenant_2
          driver: ${quarkus.datasource.jdbc.driver}
        db-kind: ${quarkus.datasource.db-kind}
        username: ${quarkus.datasource.username}
        password: ${quarkus.datasource.password}

    liquibase:
      tenant_1:
        migrate-at-start: true
        change-log: org/damap/base/db/changeLog-root.yaml
      tenant_2:
        migrate-at-start: true
        change-log: org/damap/base/db/changeLog-root.yaml
```

`tenant_1`and `tenant_2` are standins for tenant affiliations after the transformation.
`tenant-configs` hold all tenant specific config items, which can be found under `damap.tenant-aware` - they can be
used to configure DAMAP for each tenant separately.

This part of the configs needs to be added per tenant to configure the tenant datasources.
```
    datasource:
      tenant_1:
        jdbc:
          url: jdbc:postgresql://damap-db:5432/tenant_1
          driver: ${quarkus.datasource.jdbc.driver}
        db-kind: ${quarkus.datasource.db-kind}
        username: ${quarkus.datasource.username}
        password: ${quarkus.datasource.password}

    liquibase:
      tenant_1:
        migrate-at-start: true
        change-log: org/damap/base/db/changeLog-root.yaml
```

Tenants can be easily added or removed by editing this `tenants.yaml` file - DAMAP automatically runs the
[reaugmentation](https://quarkus.io/guides/reaugmentation) process when the container or pod starts up, which rebuilds
quarkus bytecode without compiling sourcecode. This process then adds and enables the newly configured tenants.

---

## Effect on configs

The `tenant-configs:` in the tenants.yaml file in the above section need to be managed per tenant, which leads to
config duplication.
DAMAP uses [ConfigProperty](https://quarkus.io/guides/config-reference#configproperty) to laod most configs, which doesnt
work for multitenancy, since its loaded statically at buildtime.
Thats why DAMAP uses [ConfigMapping](https://quarkus.io/guides/config-reference#configmapping), for configs with the
`damap.tenant-aware` prefix.
Since [ConfigMapping](https://quarkus.io/guides/config-reference#configmapping) allows us to load a config object per
tenant, we can switch between configs at runtime using the
[TenantConfigResolver](../src/main/java/org/damap/base/rest/config/domain/TenantConfigResolver.java).