#!/bin/sh
# This script generates the nginx configuration for proxying requests to Keycloak, if a Keycloak instance is detected.
# It is mounted into the nginx container and executed at startup.
# If no Keycloak instance is detected, it generates a dummy config that disables the Keycloak routes.

set -e

CONF=/etc/nginx/conf.d/keycloak.conf

if getent hosts keycloak >/dev/null 2>&1; then
    cat > "$CONF" <<'EOF'
location ~ ^/(admin|realms|resources)/ {
    proxy_pass http://keycloak:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
EOF
else
    echo "# keycloak disabled" > "$CONF"
fi
