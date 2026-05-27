# Development guide

This document contains developer documentation for the project, including setup and runtime instructions.

## Quickstart guide for development

DAMAP is made up of multiple parts - the two main ones are the backend (this repository) and the [frontend](https://github.com/damap-org/damap-frontend).
DAMAP also requires extra external services to run properly, which are provided via docker containers while developing.

To start the dockerized setup with PostgreSQL, please run the following commands:
```shell
docker compose up -d
```

By default, docker pulls back and frontend images from GitHub - the docker frontend can then be reached at
http://localhost:8085/, use user/user for username/password.
For more info about the docker setup, see [the section below](#run-with-docker-compose).

The containerized DAMAP instance is useful for testing the software and demonstrating it to others, but it is not recommended for active development.

For development, we recommend running local instances of the frontend and backend instead of using the fully containerized setup.
This has the advantage of enabling automatic live reload and removes to need to rebuild images after every code change.

As a prerequisite, the DAMAP backend requires the following:
- Java 17
- Maven

The local backend can be started using maven with the following command:

```shell
   mvn compile quarkus:dev
```

As a prerequisite, the DAMAP frontend requires the following:
- Angular ^19.2.0
- Node.js ^18.19.1 || ^20.11.1 || ^22.0.0

Start the frontend using the Angular CLI from the frontend repository:

```shell
cd ../damap-frontend
npm ci
npm start
```
The locally run frontend will now be accessible at http://localhost:4200/. 
You can log in either as a regular user or an admin with:
- user/user
- admin/admin

The local instances automatically connect to the services provided by the docker containers.

---

## Run with docker-compose

In order to set up the whole system consisting of multiple components 2 `docker-compose` files have been prepared: 

- [`docker-compose.yaml`](../docker-compose.yaml) (for develoment purposes)
- [`docker-compose.prod.yaml`](../docker-compose.prod.yaml) (for production)

The full system is comprised of the following containers

- damap-backend,
- damap-frontend,
- Keycloak (Authentication and test users user/user and admin/admin)
- Api-Mock (Used to emulate connected external services)
- Gotenberg (Creates PDF previews)
- Proxy (Intermediate between external traffic and containers)

For development, you can just start up the application with:
```shell
docker compose up -d
```

If you want to test out the dockerized production environment, refer to the [specific documentation](../docker/prod/README.md).

---

## Run with Kubernetes
TBD

---

## Multitenancy

DAMAP is a multitenant capable system, meaning one instance can serve multiple universities at once.
This is an important part of DAMAP and has to be kept in mind when developing, especially when changing aspects about
databases, authentication, configs, etc...

How to set it up for production is explained in the [Reference Manual](https://damap.org/manual) on our website.

You can also do a mock setup for development by following these [instructions](DEV_MULTITENANCY.md).

For more information on the technical implementation, please read this [documentation](MULTITENANCY.md).

## Custom Deployment / Configuration

DAMAP is built to be customizable and allows for integration of CRIS systems, OIDC servers, etc..
For a full on guide, view the [Reference Manual](https://damap.org/manual) on our website.
For development, it's advised to apply changes to the [application.yaml](../src/main/resources/application.yaml) file directly
if you wish to test something out.
The [example.env](../example.env) file holds all environment variables that can be passed into DAMAP - using them
makes it easy to find the corresponding config option in [application.yaml](../src/main/resources/application.yaml).

---

## Keycloak

DAMAP authenticates using an [OIDC server](https://openid.net/developers/how-connect-works/).
For development, DAMAP comes with a preconfigured Keycloak, which acts as an OIDC server.
Keycloak can be accessed through http://localhost:8087 and you can login as admin with

```shell
username: admin
password: admin
```

The keycloak gets preconfigured by loading a [config file](../docker/sample-damap-realm-export.json).
If you want to update this config file, you can either work on the file directly, or make the changes in the Keycloak
console and then export to a json file.
To integrate the changes, be sure to rebuild keycloak by issuing:

```shell
# rebuild
docker compose -f docker-compose.postgres.yaml build keycloak

# restart keycloak
docker compose -f docker-compose.postgres.yaml up -d keycloak
```

---

## Databases
DAMAP needs a connected database to function. DAMAP comes with a preconfigured PostgreSQL and Oracle container.
The databases are built using Liquibase, which acts as a version control system for databases.
For information regarding this see the [Liquibase documentation](../src/main/resources/org/damap/base/db/Readme.md).

To work with the databases, either use a database plugin in your IDE or connect to the containers directly.

You can access the PostgreSQL CLI directly using the postgres container with:
```shell
cd docker
docker compose -f docker-compose.postgres.yaml exec damap-db psql -U damap damap
```

You can access the Oracle CLI directly using the Oracle container with:
```shell
cd docker
docker compose -f docker-compose.oracle.yaml exec damap-db sqlplus damap/pw4damap@FREEPDB1
```

---

## OpenAPI Documentation

Per default, an OpenAPI documentation will be generated. Additionally, a
Swagger-UI is available with the previously created documentation. This provides
an easy overview of the available endpoints as well as testing them. The UI for
an instance is available at `<domain>/q/swagger-ui`. Examples:

- instance setup with docker compose: http://localhost:8085/q/swagger-ui/
- local development: http://localhost:8080/q/swagger-ui/
- deployed instance: http://my-domain/q/swagger-ui

---

## Config loading

DAMAP uses the Quarkus framework, which is configured with either environment variables or the 
[application.yaml](../src/main/resources/application.yaml) file.
To access config options in the code, most of DAMAP uses [ConfigProperty](https://quarkus.io/guides/config-reference#configproperty).
Quarkus itself currently recommends [ConfigMapping](https://quarkus.io/guides/config-reference#configmapping), which we
exclusively use for configs with the `damap.tenant-aware` prefix.
This is necessary, since on a multitenant setup, we need to switch between tenants at runtime, which requires multiple
config objects existing at once.
[ConfigProperty](https://quarkus.io/guides/config-reference#configproperty) is only able to load one config statically at
startup.
To load the `damap.tenant-aware` config, import the resolver like this:

```java
@Inject TenantConfigResolver tenantConfigResolver;

this.tenantConfigResolver.getTenantAwareConfig();
```

> [!IMPORTANT]
> Always go through the `TenantConfigResolver` when loading these configs. Failing in doing so can break parts of 
> the multitenant feature and with it all cloud deployments.

---

## Word Export

We use template word files for the export. They contain placeholders structured like this `[placeholder]`.
These placeholders are then replaced with information from the DMP and CRIS systems on export.
Additionaly, we use [resource files](../src/main/resources/org/damap/base/template/FWFTemplate.resource) for predefined
text phrases which are used in the replacement process.

> [!IMPORTANT]
> We also offer admins to create their own custom templates. 
> Whenever placeholders in the Science Europe template change, the [reference manual](https://damap.org/manual/) on the website needs to be updated.
