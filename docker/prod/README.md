# Production DAMAP deployment with Docker Compose

This directory contains the files necessary for a production deployment of DAMAP with Docker Compose.

## Step 1: certificates

A production DAMAP deployment requires TLS certificates for DAMAP to work. Your OIDC server also must run using HTTPS.

Place your certificates in the following locations:

- `tls/bundle.crt` should contain your certificate bundle, including any intermediate certificates in PEM format.
- `tls/private.pem` should contain your private key in PEM format.

## Step 2: customizing the setup

Next, please take a look at the [`.env`](.env) file and change the settings in that file.

## Step 3: starting DAMAP

Now you can start DAMAP. Please note that you may have to use profiles, depending on your use case. The following profiles are available:

- `with_db`: deploys a PostgreSQL container next to DAMAP.
- `with_keycloak`: deploys Keycloak alongside DAMAP. Use this if you don't have an OIDC server.
- `with_keycloak_db`: deploys a PostgreSQL container next to Keycloak.

You can start DAMAP as follows:

```
docker compose \
    --profile with_db \
    --profile with_keycloak \
    --profile with_keycloak_db \
    up
```

> [!WARNING]
> Keycloak is set up with an initial configuration, but this configuration is not meant for a full-scale production deployment. We recommend thoroughly securing your Keycloak installation.
