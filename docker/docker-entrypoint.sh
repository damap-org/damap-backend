#!/bin/bash
#
# Script to run the Quarkus application in a Docker container.
# It re-augments the application with environment variables and then starts it.
#

set -euo pipefail

db_kind="${DAMAP_DATABASE_DB_KIND:-postgresql}"
marker_file=".db_kind"

# Check if reaugmentation is needed.
if [[ ! -f "${marker_file}" ]] || [[ "$(cat ${marker_file})" != "${db_kind}" ]]; then
    echo "docker-entrypoint.sh: [INFO] Running re-augmentation for db_kind: ${db_kind}."
    java ${JAVA_OPTIONS} -Dquarkus.launch.rebuild=true -jar quarkus-run.jar
    echo "${db_kind}" > "${marker_file}"
    echo "docker-entrypoint.sh: [INFO] Re-augmentation complete."
fi

echo "docker-entrypoint.sh: [INFO] Starting application."
exec java ${JAVA_OPTIONS} -jar quarkus-run.jar
