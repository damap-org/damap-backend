# syntax=docker/dockerfile:1

# ===== Stage 1: Build ===== #
# Create a first stage container to build the application, this container image will be dropped once
# the runner is built
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder

# This Dockerfile uses labels from the label-schema namespace from http://label-schema.org/rc1/
LABEL org.label-schema.name="DAMAP-backend" \
    org.label-schema.description="DAMAP is a tool that aims to facilitate the creation of data management plans (DMPs) for researchers." \
    org.label-schema.usage="https://github.com/tuwien-csd/damap-backend/tree/master/doc" \
    org.label-schema.vendor="Technische Universität Wien" \
    org.label-schema.url="https://github.com/tuwien-csd/damap-backend" \
    org.label-schema.vcs-url="https://github.com/tuwien-csd/damap-backend" \
    org.label-schema.schema-version="1.0" \
    org.label-schema.docker.cmd="docker run -d -p 8080:8080 damap"

ARG BUILD_HOME=/home/app

RUN mkdir -p "${BUILD_HOME}/.m2/repository" && chown -R 1000:0 "${BUILD_HOME}"

WORKDIR "${BUILD_HOME}"
USER 1000

COPY src ./src
COPY ./pom.xml .

RUN mvn \
    -Duser.home="${BUILD_HOME}" \
    -B package \
    -DskipTests \
    -Dquarkus.package.jar.type=mutable-jar

# ===== Stage 2: Runtime ===== #
# This stage will only contain the runtime binaries without build dependencies.
FROM rockylinux:8.5 AS runner

ARG JAVA_PACKAGE=java-17-openjdk-headless

# configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -Duser.home=/deployments" \ 
    LANG="en_US.UTF-8" \
    LANGUAGE="en_US:en"

WORKDIR /quarkus-app

COPY --from=builder /home/app/target/quarkus-app/ ./
COPY ./docker/docker-entrypoint.sh /docker-entrypoint.sh

RUN dnf install -y openssl tzdata-java curl ca-certificates ${JAVA_PACKAGE} \
    && dnf clean all -y \
    && chown 1001:root -R ./

EXPOSE 8080

# for Openshift based unprivilegued Kubernetes environments, we will set the user to 1001
USER 1001

ENTRYPOINT [ "/docker-entrypoint.sh" ]
