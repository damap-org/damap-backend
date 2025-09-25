# Helm chart for Kubernetes/OpenShift deployment

This Helm chart deploys [DAMAP](https://damap.org) on a Kubernetes of OpenShift cluster. Some features, such as auto-creating a custom build from Git, are only supported on OpenShift.

## Pre-requisites

In order for the deployment to succeed you should configure your Kubernetes cluster
as follows:

- You will need an ingress controller (vanilla Kubernetes only)
- Your ingress controller should be able to provision TLS certificates

## Storage (vanilla Kubernetes only)

If you do not have a storage controller that can automatically create Persistent Volumes, please create two PVs with 5 GB each. Make sure that their storage class matches the class you specify in the Helm values file in the next step.

## Configuration

All deployment settings are managed in a `values.yaml` file. Below are the main sections you should review and adapt before deploying.

#### Global

| Variable  | Description                                                                       | Default |
|-----------|-----------------------------------------------------------------------------------|---------|
| openshift | Set to true if deploying on OpenShift, false for vanilla Kubernetes.              | false   |
| debug     | Enable debug-level logging. Intended for development or troubleshooting purposes. | true    |


#### DAMAP Application

| Variable           | Description                                                                                                                                                                                                                                                                                                    | Default                                                                                                                                                                                                                                                                                      |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| frontend_image     | Frontend container image to deploy.                                                                                                                                                                                                                                                                            | ghcr.io/damap-org/damap-frontend:next                                                                                                                                                                                                                                                        |
| backend_image      | Backend container image to deploy.                                                                                                                                                                                                                                                                             | ghcr.io/damap-org/damap-backend:next                                                                                                                                                                                                                                                         |
| db_name            | Name of the database used by DAMAP.                                                                                                                                                                                                                                                                            | damap                                                                                                                                                                                                                                                                                        |
| db_user            | Database username for connecting DAMAP to its database.                                                                                                                                                                                                                                                        | damap                                                                                                                                                                                                                                                                                        |
| db_password        | Password for the database user. If empty, Helm will attempt to auto-generate one.                                                                                                                                                                                                                              | damap_pass                                                                                                                                                                                                                                                                                   |
| hostname           | Hostname where the DAMAP frontend and backend will be reachable. For local testing use `localhost`.                                                                                                                                                                                                            | localhost                                                                                                                                                                                                                                                                                    |
| protocol           | Protocol to use for accessing DAMAP (`http` or `https`).                                                                                                                                                                                                                                                       | http                                                                                                                                                                                                                                                                                         |
| storage_class_name | Kubernetes StorageClass used for persistent volumes. Leave empty to use the default StorageClass.                                                                                                                                                                                                              |                                                                                                                                                                                                                                                                                              |
| create_pv          | If true, Helm will create PersistentVolumes using a hostPath (for demo or small-scale deployments only). Not recommended for production.                                                                                                                                                                       | true                                                                                                                                                                                                                                                                                         |
| host_folder        | Local directory used as the base for hostPath PVs (if `create_pv` is true).                                                                                                                                                                                                                                    | /tmp/damap                                                                                                                                                                                                                                                                                   |
| person_services    | A prioritized list of external services DAMAP will use to resolve and enrich person (researcher) information.<br>Each entry includes:<ul><li>Human-readable name shown in the UI</li><li>Internal identifier for the service.class-name</li><li>Fully qualified Java class implementing the service.</li></ul> | - display-text: "Pure"<br>&nbsp;&nbsp;query-value: "PURE"<br>&nbsp;&nbsp;class-name: "org.damap.base.integration.pure.PurePersonService"<br>- display-text: "ORCID"<br>&nbsp;&nbsp;query-value: "ORCID"<br>&nbsp;&nbsp;class-name: "org.damap.base.integration.orcid.ORCIDPersonServiceImpl" |
| projects_service   | Defines the service used to fetch and manage project metadata. This must match a supported integration provider (e.g., `elsevier-pure`).                                                                                                                                                                       | elsevier-pure                                                                                                                                                                                                                                                                                |

#### Authentication (OIDC)

| Variable           | Description                                                                                                                        | Default                                                                     |
|--------------------|------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| frontend_endpoint  | URL the DAMAP frontend (browser) will use to communicate with the OIDC provider. Must be accessible from the user’s machine.       | https://                                                                    |
| backend_endpoint   | URL the DAMAP backend will use to communicate with the OIDC provider. Can differ from the frontend if needed for internal routing. | https://                                                                    |
| backend_client_id  | OIDC client ID configured for the backend application. Must match the registration on the OIDC server.                             | damap                                                                       |
| frontend_client_id | OIDC client ID configured for the frontend application. Must match the registration on the OIDC server.                            | damap                                                                       |
| scope              | List of OIDC scopes requested by the DAMAP client. Common scopes include openid, profile, email, offline_access, roles, personID.  | [openid, profile, email, offline_access, microprofile-jwt, roles, personID] |
| person_id_claim    | Name of the claim used as a unique identifier for a person in DAMAP.                                                               | personID                                                                    |


#### OIDC Server

| Variable           | Description                                                                             | Default       |
|--------------------|-----------------------------------------------------------------------------------------|---------------|
| deploy             | Set to true to deploy Keycloak via Helm. Set to false to use an external OIDC server.   | true          |
| admin_user         | Username of the Keycloak administrator.                                                 | admin         |
| admin_password     | Password for the Keycloak administrator. If empty, a secret is generated automatically. |               |
| db_name            | Database name used by Keycloak.                                                         | keycloak      |
| db_user            | Database user for Keycloak.                                                             | keycloak      |
| db_password        | Password for the Keycloak database user.                                                | keycloak123   |
| hostname           | Hostname where Keycloak will be accessible.                                             | localhost     |
| protocol           | Protocol to use for Keycloak (http or https).                                           | http          |
| storage_class_name | StorageClass used for Keycloak PVs. Leave empty for default.                            |               |
| create_pv          | If true, Helm will create hostPath PVs for Keycloak (demo/local use only).              | true          |
| host_folder        | Directory used for Keycloak hostPath PVs if `create_pv` is true.                        | /tmp/keycloak |


#### Elsevier Pure Integration

| Variable                    | Description                                                                                                  | Default                              |
|-----------------------------|--------------------------------------------------------------------------------------------------------------|--------------------------------------|
| enabled                     | Enable integration with Elsevier Pure.                                                                       | true                                 |
| backend                     | `file` uses mock data for testing, `http` uses the real Pure API.                                            | file                                 |
| endpoint                    | Pure API endpoint URL. Required when backend=http.                                                           | https://...                          |
| api_key                     |                                                                                                              | ...                                  |
| description_classification  | Classification URI for project descriptions from Pure.                                                       | /dk/atira/pure/...                   |
| project_lead_classification | Classification URI for project leads.                                                                        | /dk/atira/pure/...                   |
| role_classifications        | Mapping of Pure role classifications to DAMAP roles. Refer to DAMAP documentation for supported role values. | /dk/atira/pure/... :  PROJECT_MEMBER |


#### Frontend Customization

| Variable     | Description                                                                                     | Default |
|--------------|-------------------------------------------------------------------------------------------------|---------|
| customize    | Set to true to enable a custom logo.                                                            | false   |
| logo         | Base64-encoded SVG for the main logo. Combined with `logo_cropped`, total must not exceed 1 MB. |         |
| logo_cropped | Base64-encoded SVG for the cropped logo. Combined with `logo`, total must not exceed 1 MB.      |         |


## Deployment

Once you have reviewed and customized the `values.yaml` file, you can deploy DAMAP into your Kubernetes or OpenShift cluster using Helm.

Install or upgrade the release:
```bash 
helm upgrade --install --values values.yaml <release_name> ./
```

To override specific values without editing the file, use the `--set` flag:
```bash
helm upgrade --install \
  --set damap.hostname=damap.example.org \
  --set damap.protocol=https \
  --values values.yaml \
  <release_name> ./
```

If you deploy Keycloak (`keycloak.deploy: true`), its admin credentials are stored in a Kubernetes secret. Retrieve them with:
```bash
kubectl get secret keycloak -o jsonpath='{.data.admin-password}' | base64 -d
```

💡 This approach applies to all password-based variables defined by the chart.

## Development & Local Testing
You can deploy DAMAP on a local [KinD](https://kind.sigs.k8s.io/) cluster for end-to-end testing:
```bash
./test/create-test-cluster.sh                        # Create a test cluster.
helm install --values values.yaml <release_name> ./  # Install the chart.
```

## Common issues and solutions
1. **Backend fails to start** – This usually happens because Keycloak takes longer to become ready than the backend expects. Once Keycloak is up, restart the backend pod manually. Once Keycloak is fully up, restarting the backend pod will typically resolve the issue.

2. **Authentication page fails to load** – If you access the frontend too quickly, Keycloak may still be starting up and unable to serve OIDC requests. Wait until Keycloak is fully ready before visiting the auth page.

    💡 Tip: Review and adjust the resource requests and limits in the YAML manifests to ensure Keycloak has enough CPU and memory to start within the expected time.

3. **Passwords mismatch after redeployment** – When using KinD or hostPath volumes locally, deleting a Helm release does not remove the underlying database files. If you rely on auto-generated passwords, redeploying may fail because the persisted database expects the old password.

    Solutions:
      - Manually delete the local volume data (`host_path` paths in your KinD node) before redeploying.
      - Alternatively, set fixed passwords in your `values.yaml` for stable local testing.
