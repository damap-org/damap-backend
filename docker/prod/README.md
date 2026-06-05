# Production DAMAP deployment with Docker Compose

This directory contains the files necessary for a production deployment of DAMAP with Docker Compose.

> [!WARNING]
> Review the security, backup, TLS, and identity-provider settings before using this setup in production.

## Step 1: Certificates

A production DAMAP deployment requires TLS certificates. Your OIDC server must also run using HTTPS.

Place your certificates in the following locations:

- `tls/bundle.crt` should contain your certificate bundle, including any intermediate certificates in PEM format.
- `tls/private.pem` should contain your private key in PEM format.

For local testing only, you can generate a self-signed certificate pair from the repository root:

```shell
docker/prod/scripts/generate-tls-pair.sh
```

## Step 2: Configure the setup

Docker Compose reads configuration from environment variables.
You can export them in your shell or create a `.env` file in this directory.
The repository-level `example.env` documents the available variables and can be copied as a starting point.

Required variables for DAMAP:

| Variable | Description |
| --- | --- |
| `DAMAP_HOSTNAME` | Public hostname used by DAMAP and bundled Keycloak |
| `DAMAP_QUARKUS_HTTP_CORS_ORIGINS` | Allowed frontend origin, for example `https://damap.example.org` |
| `DAMAP_DB_HOST` | DAMAP database host, or `damap-db` when using `with_db` |
| `DAMAP_DB_NAME` | DAMAP database name |
| `DAMAP_DB_USERNAME` | DAMAP database user |
| `DAMAP_DB_PASSWORD` | DAMAP database password |
| `DAMAP_AUTH_USER_ROLES_CLAIM_PATH` | OIDC claim path for user roles, for example `realm_access/roles` |
| `DAMAP_QUARKUS_OIDC_AUTH_SERVER_URL` | OIDC server URL used by the backend |
| `DAMAP_QUARKUS_OIDC_CLIENT_ID` | OIDC client ID |
| `DAMAP_QUARKUS_OIDC_TOKEN_ISSUER` | Expected token issuer URL |
| `PROXY_LOGS_HOST_PATH` | Host path for nginx logs |

The backend image supports only `postgresql` and `oracle` as database kinds.
Set `DAMAP_DB_KIND` to select the database kind; it defaults to `postgresql`.
The Docker entrypoint then builds the Quarkus JDBC URL automatically from `DAMAP_DB_HOST`, `DAMAP_DB_PORT`, and `DAMAP_DB_NAME`.
For Oracle, set `DAMAP_DB_KIND=oracle`; the entrypoint also sets the Oracle JDBC driver and Hibernate dialect.

Optional general variables:

| Variable | Description |
| --- | --- |
| `DAMAP_VERSION` | Backend and frontend image tag, defaults to `next` |
| `DAMAP_DB_KIND` | Database kind, either `postgresql` or `oracle`; defaults to `postgresql` |

Required variables when using the bundled DAMAP database:

| Variable | Description |
| --- | --- |
| `DAMAP_DB_DATA_HOST_PATH` | Host path for DAMAP PostgreSQL data |

Required variables when using bundled Keycloak:

| Variable | Description |
| --- | --- |
| `KC_ADMIN_USERNAME` | Keycloak admin username |
| `KC_ADMIN_PASSWORD` | Keycloak admin password |
| `KC_DB_DATABASE` | Keycloak database name |
| `KC_DB_USERNAME` | Keycloak database user |
| `KC_DB_PASSWORD` | Keycloak database password |
| `KC_DB_DATA_HOST_PATH` | Host path for Keycloak PostgreSQL data |

Optional integration variables such as Pure endpoint settings can also be supplied through the environment.
Review `docker-compose.yaml` for the complete list of supported variables and defaults.

## Step 3: Choose profiles

The production compose setup can use external services or start bundled services via profiles:

- `with_db`: deploys a PostgreSQL container next to DAMAP.
- `with_keycloak`: deploys Keycloak alongside DAMAP. Use this if you don't have an OIDC server.
- `with_keycloak_db`: deploys a PostgreSQL container next to Keycloak.

The `with_db` profile deploys PostgreSQL only.
To use Oracle in production, provide an external Oracle database and set `DAMAP_DB_KIND=oracle` together with the matching `DAMAP_DB_HOST`, `DAMAP_DB_PORT`, and `DAMAP_DB_NAME`.

Common combinations:

| Use case | Profiles |
| --- | --- |
| External database and external OIDC provider | none |
| Bundled DAMAP database and external OIDC provider | `with_db` |
| External DAMAP database and bundled Keycloak with bundled Keycloak database | `with_keycloak`, `with_keycloak_db` |
| Fully bundled Docker Compose stack | `with_db`, `with_keycloak`, `with_keycloak_db` |

## Step 4: Start DAMAP

From this directory, start the fully bundled stack with:

```shell
docker compose \
  --profile with_db \
  --profile with_keycloak \
  --profile with_keycloak_db \
  up -d
```

> [!WARNING]
> Keycloak is set up with an initial configuration, but this configuration is not meant for a full-scale production deployment. We recommend thoroughly securing your Keycloak installation.

You can also use the root-level production shortcut from the repository root:

```shell
docker compose -f docker-compose.prod.yaml \
  --profile with_db \
  --profile with_keycloak \
  --profile with_keycloak_db \
  up -d
```

When using the bundled Keycloak profile, Keycloak is exposed through the DAMAP hostname.
The Keycloak admin console is available under `/admin/`, for example `https://damap.example.org/admin/`.
Keep the trailing slash.

## Operations

Check service status:

```shell
docker compose ps
```

View logs:

```shell
docker compose logs -f
```

Stop DAMAP:

```shell
docker compose down
```

Do not remove host-mounted database directories unless you intentionally want to delete persisted data.
