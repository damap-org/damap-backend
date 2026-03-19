#!/bin/bash
#
# Docker entrypoint to run the Quarkus application.
# It sets default database configuration values based on environment variables.
# It then re-augments the application and starts it.

set -euo pipefail

log() {
	# Usage: log "message" "LEVEL"
	local msg="$1"
	local level="${2:-INFO}"
	echo "$(date +"%Y-%m-%d %H:%M:%S,%3N") $level  [docker-entrypoint.sh] $msg"
}

# Require DB kind to be explicitly set.
: "${QUARKUS_DATASOURCE_DB_KIND:?QUARKUS_DATASOURCE_DB_KIND must be set. Supported: oracle, postgresql.}"

case "$QUARKUS_DATASOURCE_DB_KIND" in
postgresql)
	log "Using database configuration for PostgreSQL."
	export QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://${DAMAP_DB_HOST:-postgres-db}:${DAMAP_DB_PORT:-5432}/${DAMAP_DB_NAME:-damap}"
	# driver and dialect already handled by application.yaml
	;;
oracle)
	log "Using database configuration for Oracle."
	export QUARKUS_DATASOURCE_JDBC_URL="jdbc:oracle:thin:@${DAMAP_DB_HOST:-oracle-db}:${DAMAP_DB_PORT:-1521}/${DAMAP_DB_NAME:-FREEPDB1}"
	export QUARKUS_DATASOURCE_JDBC_DRIVER="oracle.jdbc.driver.OracleDriver"
	export QUARKUS_HIBERNATE_ORM_DIALECT="org.hibernate.dialect.OracleDialect"
	;;
*)
	log "Unknown QUARKUS_DATASOURCE_DB_KIND: $QUARKUS_DATASOURCE_DB_KIND. Supported: oracle, postgresql." "ERROR"
	exit 1
	;;
esac

log "Running Quarkus re-augmentation."
java ${JAVA_OPTIONS} -Dquarkus.launch.rebuild=true -jar quarkus-run.jar

log "Starting application."
exec java ${JAVA_OPTIONS} -jar quarkus-run.jar
