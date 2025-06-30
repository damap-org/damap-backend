# Installation

## Quickstart guide for development

DAMAP is made up of two parts: a backend (this repository) and a [frontend](https://github.com/damap-org/damap-frontend).
You will probably need to set up both of these for development. DAMAP also requires extra services, which are provided via
docker containers.

To start the dockerized setup with PostgreSQL, please run the following commands:
```shell
cd docker
docker compose -f docker-compose.dev.postgres.yaml up
```

By default, docker pulls back and frontend
images from GitHub - the docker frontend can then be reached at http://localhost:8085/, use user/user for username/password.
For more info about the docker setup, see [the section below](#run-with-docker-compose). The containerized DAMAP instance
is good for testing out the software and showing it to others, but not for development. For development, it is better to
run local instances of the front and backend. The backend can be started using maven with the following command:

```shell
   mvn compile quarkus:dev
```

Start the frontend using the Angular CLI from the frontend repository:

```shell
cd ../damap-frontend
npm ci
npm start
```
The locally run frontend will now be accessible at http://localhost:4200/, same username and password. The local instances
will also automatically connect to the services provided by the docker containers. Now you have a running DAMAP setup with
autoreload on changes to the source files.


## Run with docker-compose

In order to set up the whole system consisting of multiple components 3
`docker-compose` files have been prepared: `docker-compose.oracle.yaml`, `docker-compose.postgres.yaml` and `docker-compose.common.yaml`.
With this file it should be
straight forward to get a sample system up and running.

The full system will be comprised of

- damap-backend,
- damap-frontend,
- dummy Keycloak with login name "user" and password "user"
- dummy PostgreSQL database
- and dummy APIs providing person and project data

To start up the cluster of components with PostgreSQL, you can use the following command:

```shell
cd docker
docker compose -f docker-compose.dev.postgres.yaml up
```

Alternatively, if you wish you to start the dockerized setup with Oracle, the following commands will allow you to do so.
Note that you need to modify the dev profile in the [application.yaml](src/main/resources/application.yaml) file as suggested by the comments.
```shell
cd docker
docker compose -f docker-compose.dev.oracle.yaml up
```

See the documented sections in the [docker-compose.postgres.yaml](docker/docker-compose.dev.postgres.yaml) and in the [docker-compose.oracle.yaml](docker/docker-compose.dev.oracle.yaml) to make further
configurations respectively.

### Keycloak

Keycloak can be accessed through http://localhost:8087 and you can login
to keycloak as admin with

```shell
username: admin
password: admin
```

### Update Realm config

If you update a running Keycloak instance, by adding users, changing properties
a.s.o., you can export the current configuration to a Json file.
Save this Json file to [keycloak export file](docker/sample-damap-realm-export.json)
to integrate it within the docker-compose "cluster". Be sure to rebuild keycloak
by issuing:

```shell
# rebuild
docker compose -f docker-compose.dev.postgres.yaml build keycloak

# restart keycloak
docker compose -f docker-compose.dev.postgres.yaml up -d keycloak
```

### PostgreSQL

You can access the PostgreSQL CLI directly using the postgres container with:

```shell
cd docker
docker compose -f docker-compose.dev.postgres.yaml exec damap-db psql -U damap damap
```

### Oracle
You can access the Oracle CLI directly using the Oracle container with:
```shell
cd docker
docker compose -f docker-compose.dev.oracle.yaml exec damap-db sqlplus damap/pw4damap@FREEPDB1
```

## Custom Deployment

In order to adapt the project and deploy it in your own institutional environment
follow the [custom deployment instructions](CUSTOMISING.md).

## OpenAPI Documentation

Per default, an OpenAPI documentation will be generated. Additionally, a
Swagger-UI is available with the previously created documentation. This provides
an easy overview of the available endpoints as well as testing them. The UI for
an instance is available at `<domain>/q/swagger-ui`. Examples:

- instance setup with docker compose: http://localhost:8085/q/swagger-ui/
- local development: http://localhost:8080/q/swagger-ui/
- deployed instance: http://my-domain/q/swagger-ui
