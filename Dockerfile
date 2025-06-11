# syntax=docker/dockerfile:1

# Create a first stage container to build the application, this container image will be dropped once
# the runner is built
FROM maven:3.9.5-eclipse-temurin-17-alpine as builder

# This Dockerfile uses labels from the label-schema namespace from http://label-schema.org/rc1/
LABEL maintainer="zeno.casellato@tuwien.ac.at" \
        org.label-schema.name="DAMAP-backend" \
        org.label-schema.description="DAMAP is a tool that aims to facilitate the creation of data management plans (DMPs) for researchers." \
        org.label-schema.usage="https://github.com/tuwien-csd/damap-backend/tree/master/doc" \
        org.label-schema.vendor="Technische Universität Wien" \
        org.label-schema.url="https://github.com/tuwien-csd/damap-backend" \
        org.label-schema.vcs-url="https://github.com/tuwien-csd/damap-backend" \
        org.label-schema.schema-version="1.0" \
        org.label-schema.docker.cmd="docker run -d -p 8080:8080 damap"

ARG BUILD_HOME=/home/app
ARG BUILD_PROFILE=postgres

RUN mkdir $BUILD_HOME && mkdir -p $BUILD_HOME/.m2/repository && chown -R 1000:0 $BUILD_HOME
USER 1000
WORKDIR $BUILD_HOME
COPY src ./src
COPY ./pom.xml .

VOLUME ["/home/app/.m2/repository"]
RUN mvn -Duser.home=$BUILD_HOME -B package -DskipTests -Dquarkus.profile=${BUILD_PROFILE}

# Create a second stage container which will only contain the runtime binaries without build dependencies
FROM rockylinux:8.5 as runner

ARG JAVA_PACKAGE=java-17-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8

# path to copy built binaries from builder container
ARG BUILD_HOME=/home/app

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# install java and the run-java script and set up permissions for the unprivileged 1001 container user
RUN dnf install -y openssl tzdata-java curl ca-certificates ${JAVA_PACKAGE} \
    && dnf clean all -y \
    && mkdir /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/lib/security/java.security \
    && chmod 0755 /deployments \
    && chmod 777 /deployments/run-java.sh \
    && chown -R 1001:root /deployments

# configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -Duser.home=/deployments"

# copy runtime binaries to /deployments folder on runner container, the run-java script will pick this up
# and start the application
COPY --from=builder $BUILD_HOME/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder $BUILD_HOME/target/quarkus-app/*.jar /deployments/
COPY --from=builder $BUILD_HOME/target/quarkus-app/app/ /deployments/app/
COPY --from=builder $BUILD_HOME/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080

# for Openshift based unprivilegued Kubernetes environments, we will set the user to 1001
USER 1001

ENTRYPOINT [ "/deployments/run-java.sh" ]
