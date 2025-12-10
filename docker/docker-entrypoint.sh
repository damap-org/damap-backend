#!/bin/bash
#
# Script to run the Quarkus application in a Docker container.
# It re-augments the application with environment variables and then starts it.
#

set -euo pipefail

echo "$(date +"%Y-%m-%d %H:%M:%S,%3N") INFO  [docker-entrypoint.sh] Running Quarkus re-augmentation."
java ${JAVA_OPTIONS} -Dquarkus.launch.rebuild=true -jar quarkus-run.jar

echo "$(date +"%Y-%m-%d %H:%M:%S,%3N") INFO  [docker-entrypoint.sh] Starting application."
exec java ${JAVA_OPTIONS} -jar quarkus-run.jar
