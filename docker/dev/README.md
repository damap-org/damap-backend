# Development DAMAP deployment with Docker Compose

This directory contains the Docker Compose setup used for local development and demos.
It starts DAMAP with the backend, frontend, PostgreSQL, Keycloak, API mock services, Gotenberg, and nginx.

> [!WARNING]
> This setup is intended for development and testing only. Do not use it for production deployments.

## Quick start

From the repository root, run:

```shell
docker compose up -d
```

This uses the shortcut compose file at the repository root, which includes `docker/dev/docker-compose.yaml`.

You can also start the setup directly from this directory:

```shell
cd docker/dev
docker compose up -d
```

## Access

- DAMAP frontend: http://localhost:8085/
- Keycloak: http://localhost:8087/
- API mock: http://localhost:8091/
- Gotenberg: http://localhost:3000/
- PostgreSQL: `localhost:8088`

Sample DAMAP users:

- `user/user`
- `admin/admin`

Keycloak admin user:

- `admin/admin`

## Database profiles

PostgreSQL starts by default. To make the selected database explicit, use:

```shell
docker compose --profile with_postgres up -d
```

To start the setup with Oracle instead, use:

```shell
DAMAP_DB_KIND=oracle docker compose --profile with_oracle up -d
```

The backend image supports only `postgresql` and `oracle` as database kinds.
Its Docker entrypoint builds the Quarkus JDBC URL automatically from `DAMAP_DB_KIND`, `DAMAP_DB_HOST`, `DAMAP_DB_PORT`, and `DAMAP_DB_NAME`.
When `DAMAP_DB_KIND=oracle`, the entrypoint also sets the Oracle JDBC driver and Hibernate dialect.

Both database containers expose their database port on host port `8088`, so only one database profile should be active at a time.

## Local images

The backend service uses `ghcr.io/damap-org/damap-backend:next`.
When running from this repository, Compose can build that image from the repository root.

The frontend service uses `ghcr.io/damap-org/damap-frontend:next`.
If you want Compose to build the frontend locally, check out `damap-frontend` next to this repository:

```text
damap-backend/
damap-frontend/
```

## Useful commands

Stop the setup:

```shell
docker compose down
```

Stop the setup and remove development database volumes:

```shell
docker compose down --volumes
```

Open a PostgreSQL shell:

```shell
docker compose exec postgres-db psql -U damap damap
```

Open an Oracle SQL shell:

```shell
DAMAP_DB_KIND=oracle docker compose --profile with_oracle exec oracle-db sqlplus damap/pw4damap@FREEPDB1
```
