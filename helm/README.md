# Helm chart for Kubernetes/OpenShift deployment

This Helm chart deploys [DAMAP](https://damap.org) on Kubernetes or OpenShift cluster. Some features, such as auto-creating a custom build from Git, are only supported on OpenShift.

> [!WARNING]
> This chart is currently marked as a pre-release and targets DAMAP 5.0.0-rc1. It is intended for evaluation and testing.


## Pre-requisites

Before deploying:

- Kubernetes ≥ 1.25
- An Ingress controller (vanilla Kubernetes)
- TLS certificate provisioning (recommended for production)
- Persistent storage (or hostPath for local testing)

## Storage (vanilla Kubernetes only)

If you do not have a storage controller that can automatically create Persistent Volumes, create two PVs with 5 GB each. Make sure that their storage class matches the class you specify in the Helm values file.

## Installation (OCI)

This chart is published as an OCI artifact.

Install:

```bash
helm install damap oci://ghcr.io/damap-org/damap --version 0.1.0-rc1
```

Upgrade:

```bash
helm upgrade damap oci://ghcr.io/damap-org/damap --version 0.1.0-rc1
```

## Configuration

All configuration is managed via `values.yaml`. Below are the main sections you should review and adapt before deploying.

#### Global

| Variable  | Description                                                                       | Default |
|-----------|-----------------------------------------------------------------------------------|---------|
| debug     | Enable debug-level logging. Intended for development or troubleshooting purposes. | true    |
| openshift | Set to true if deploying on OpenShift, false for vanilla Kubernetes.              | false   |


#### OpenShift Custom Builds (`customBuild`)

| Key                       | Description                                     | Default                                     |
|---------------------------|-------------------------------------------------|---------------------------------------------|
| backend.name              | Name of the OpenShift BuildConfig for backend.  |                                           |
| backend.ref               | Git branch or tag to build.                     | next                                        |
| backend.repo              | Git repository for backend build.               | https://github.com/damap-org/damap-backend  |
| frontend.name             | Name of the OpenShift BuildConfig for frontend. |                                           |
| customBuild.frontend.ref  | Git branch or tag to build.                     | next                                        |
| customBuild.frontend.repo | Git repository for frontend build.              | https://github.com/damap-org/damap-frontend |

#### DAMAP Application (`damap`)

| Variable          | Description                                                                                                                                                                                                                                                                                                  | Default                                                                                                                                                                                                                                              |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| backendVersion    | Backend image tag                                                                                                                                                                                                                                                                                            | next                                                                                                                                                                                                                                                 |
| customDomain      | Use custom domain routing                                                                                                                                                                                                                                                                                    | false                                                                                                                                                                                                                                                |
| dbName            | Database name                                                                                                                                                                                                                                                                                                | damap                                                                                                                                                                                                                                                |
| dbPassword        | Database password                                                                                                                                                                                                                                                                                            | damap_pass                                                                                                                                                                                                                                           |
| dbUser            | Database user                                                                                                                                                                                                                                                                                                | damap                                                                                                                                                                                                                                                |
| frontendVersion   | Frontend image tag                                                                                                                                                                                                                                                                                           | next                                                                                                                                                                                                                                                 |
| hostFolder        | Base directory for `hostPath` (demo only)                                                                                                                                                                                                                                                                    | /tmp/damap                                                                                                                                                                                                                                           |
| hostname          | Public hostname                                                                                                                                                                                                                                                                                              | localhost                                                                                                                                                                                                                                            |
| ingressClass      | Ingress class name.                                                                                                                                                                                                                                                                                          |                                                                                                                                                                                                                                                      |
| persistData       | Enable persistent storage                                                                                                                                                                                                                                                                                    | false                                                                                                                                                                                                                                                |
| personServices    | A prioritized list of external services DAMAP will use to resolve and enrich person (researcher) information. Each entry includes: <ul><li>Human-readable name shown in the UI</li><li>Internal identifier for the service.class-name</li><li>Fully qualified Java class implementing the service.</li></ul> | - display-text: "Pure"<br>  query-value: "PURE"<br>  class-name: "org.damap.base.integration.pure.PurePersonService"<br>- display-text: "ORCID"<br>  query-value: "ORCID"<br>  class-name: "org.damap.base.integration.orcid.ORCIDPersonServiceImpl" |
| projectsService   | Defines the service used to fetch and manage project metadata. This must match a supported integration provider (e.g.,   `elsevier-pure`)                                                                                                                                                                    | elsevier-pure                                                                                                                                                                                                                                        |
| protocol          | Protocol to use for accessing DAMAP (`http` or `https`).                                                                                                                                                                                                                                                     | http                                                                                                                                                                                                                                                 |
| storageClassName  | StorageClass for persistent volumes.                                                                                                                                                                                                                                                                         |                                                                                                                                                                                                                                                      |
| tlsClusterIssuer  | cert-manager ClusterIssuer for TLS.                                                                                                                                                                                                                                                                          |                                                                                                                                                                                                                                                      |
| useExistingSecret | Use pre-created Kubernetes Secret instead of generating one.                                                                                                                                                                                                                                                 | false                                                                                                                                                                                                                                                |


#### DAMAP Multitenancy (`damap.multitenancy`)

| Key                 | Description                                 | Default                |
|---------------------|---------------------------------------------|------------------------|
| autoCreateDatabases | Automatically create databases for tenants. | false                  |
| enabled             | Enable multi-tenant mode.                   | false                  |
| tenants             | List of tenant identifiers.                 | -tenant_1<br>-tenant_2 |


#### Frontend Customization (`frontend`)

| Variable     | Description                                                                                     | Default |
|--------------|-------------------------------------------------------------------------------------------------|---------|
| customize    | Set to true to enable a custom logo.                                                            | false   |
| logo         | Base64-encoded SVG for the main logo. Combined with `logo_cropped`, total must not exceed 1 MB. |         |
| logo_cropped | Base64-encoded SVG for the cropped logo. Combined with `logo`, total must not exceed 1 MB.      |         |


#### OIDC Server (`keycloak`)

> [!TIP]
> Set `keycloak.deploy=false` to use an external OIDC provider.

| Key               | Description                                                  | Default       |
|-------------------|--------------------------------------------------------------|---------------|
| adminPassword     | Admin password (auto-generated if empty).                    |               |
| adminUser         | Username of the Keycloak administrator.                      | admin         |
| customDomain      | Use custom domain routing.                                   | false         |
| dbName            | Keycloak database name.                                      | keycloak      |
| dbPassword        | DB password (auto-generated if empty).                       | keycloak123   |
| dbUser            | Keycloak DB user.                                            | keycloak      |
| deploy            | Deploy bundled Keycloak instance.                            | true          |
| hostFolder        | Base directory for hostPath (demo only).                     | /tmp/keycloak |
| hostname          | Public hostname                                              | localhost     |
| ingressClass      | Ingress class name.                                          |               |
| persistData       | Enable persistent storage                                    | false         |
| protocol          | Protocol to use for accessing Keycloak (`http` or `https`).  | http          |
| storageClassName  | StorageClass for persistent volumes.                         |               |
| tlsClusterIssuer  | cert-manager ClusterIssuer for TLS.                          |               |
| useExistingSecret | Use pre-created Kubernetes Secret instead of generating one. | false         |


#### Authentication (`oidc`)

| Key                | Description                                                                                                                        | Default                                                                     |
|--------------------|------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| adminRoleName      | Role name used to identify DAMAP admins                                                                                            | Damap Admin                                                                 |
| affiliationsClaim  | Affiliations claim.                                                                                                                | affiliation                                                                 |
| clientId           | OIDC client ID.                                                                                                                    | damap                                                                       |
| emailClaim         | Email claim.                                                                                                                       | email                                                                       |
| familyNameClaim    | Family name claim.                                                                                                                 | family_name                                                                 |
| givenNameClaim     | Given name claim.                                                                                                                  | given_name                                                                  |
| issuer             | URL the DAMAP frontend (browser) will use to communicate with the OIDC provider. Must be accessible from the user’s machine.       | https://...                                                                 |
| nameClaim          | Full name claim.                                                                                                                   | name                                                                        |
| scope              | List of OIDC scopes requested by the DAMAP client. Common scopes include openid, profile, email, offline_access, roles, personID.  | [openid, profile, email, offline_access, microprofile-jwt, roles, personID] |
| serverUrl          | URL the DAMAP backend will use to communicate with the OIDC provider. Can differ from the frontend if needed for internal routing. | https://...                                                                 |
| userIdClaim        | Claim containing user ID.                                                                                                          | personID                                                                    |
| userRolesClaimPath | Claim for the user roles                                                                                                           | realm_access/roles                                                          |                                                                |


#### Elsevier Pure Integration (`pure`)

| Key                       | Description                                                                                                                                                                                                                                                                                                                                                                                                                                | Default                            |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------|
| apiKey                    | API key for authentication.                                                                                                                                                                                                                                                                                                                                                                                                                | ...                                |
| backend                   | `file` (mock) or `http` (real API)                                                                                                                                                                                                                                                                                                                                                                                                         | file                               |
| descriptionClassification | Classification URI for project descriptions.                                                                                                                                                                                                                                                                                                                                                                                               | /dk/atira/pure/...                 |
| enabled                   | Enable integration with Elsevier Pure.                                                                                                                                                                                                                                                                                                                                                                                                     | true                               |
| endpoint                  | The API endpoint to use. This should be the base URL, e.g., "https://your-pure-instance.com/api".                                                                                                                                                                                                                                                                                                                                          | https://...                        |
| personsData               | Mock persons data. Used when backend is `file`.                                                                                                                                                                                                                                                                                                                                                                                            | See `values.yaml`                  |
| projectLeadClassification | Classification URI for project lead role.                                                                                                                                                                                                                                                                                                                                                                                                  | /dk/atira/pure/...                 |
| projectsData              | Mock projects data. Used when backend is `file`.                                                                                                                                                                                                                                                                                                                                                                                           | See `values.yaml`                  |
| roleClassifications       | Map Pure classification URIs to DAMAP role constants. Supported DAMAP roles include: DATA_COLLECTOR, DATA_CURATOR, DATA_MANAGER, DISTRIBUTOR, EDITOR, HOSTING_INSTITUTION, PRODUCER, PROJECT_LEADER, PROJECT_MANAGER, PROJECT_MEMBER, REGISTRATION_AGENCY, REGISTRATION_AUTHORITY, RELATED_PERSON, RESEARCHER, RESEARCH_GROUP, RIGHTS_HOLDER, SPONSOR, SUPERVISOR, WORK_PACKAGE_LEADER, PRINCIPAL_INVESTIGATOR, PROJECT_COORDINATOR, OTHER | /dk/atira/pure/...: PROJECT_MEMBER |


## Secrets

If `useExistingSecret=true`, credentials must be provided externally.

#### Using sealed secrets

1. Creating a secret (example values):

    ```bash
    oc create secret generic damap-secret \
      --from-literal=dbDatabase=damap
      --from-literal=dbKind=postgresql \
      --from-literal=dbPassword=damap_pass
      --from-literal=dbUsername=damap \
      --dry-run=client -o yaml > damap-base-secret.yaml
    ```

2. Seal the secret:

    ```bash
    kubeseal \
      --controller-namespace=sealed-secrets-controller \
      --controller-name=sealed-secrets \
      --format yaml \
      --namespace <NAMESPACE> \
      < secret-base.yaml > damap-sealed-secret.yaml
    ```

3. Commit the resulting file

## Argo CD Deployment (GitOps)

Example Argo CD Application:

```bash
spec:
  source:
    repoURL: oci://ghcr.io/damap-org
    chart: damap
    targetRevision: 0.1.0-rc1
    helm:
      valueFiles:
        - values.yaml
```

> [!WARNING]
> Since this is a prerelease, you must specify the exact version.

#### Keycloak Credentials

If you deploy Keycloak (`keycloak.deploy: true`), its admin credentials are stored in a Kubernetes secret. Retrieve them with:
```bash
kubectl get secret keycloak -o jsonpath='{.data.admin-password}' | base64 -d
```

> [!TIP]
> This approach applies to all password-based variables defined by the chart.

## Development & Local Testing
You can deploy DAMAP on a local [KinD](https://kind.sigs.k8s.io/) cluster for end-to-end testing:
```bash
./test/create-test-cluster.sh                        # Create a test cluster.
helm install --values values.yaml <RELEASE_NAME> ./  # Install the chart.
```

## Common issues and solutions
1. **Backend fails to start** – This usually happens because Keycloak takes longer to become ready than the backend expects. Once Keycloak is up, restart the backend pod manually. Once Keycloak is fully up, restarting the backend pod will typically resolve the issue.

2. **Authentication page fails to load** – If you access the frontend too quickly, Keycloak may still be starting up and unable to serve OIDC requests. Wait until Keycloak is fully ready before visiting the auth page.

    > [!TIP]
    > Review and adjust the resource requests and limits in the YAML manifests to ensure Keycloak has enough CPU and memory to start within the expected time.

3. **Passwords mismatch after redeployment** – When using KinD or hostPath volumes locally, deleting a Helm release does not remove the underlying database files. If you rely on auto-generated passwords, redeploying may fail because the persisted database expects the old password.

    Solutions:
      - Manually delete the local volume data (`host_path` paths in your KinD node) before redeploying.
      - Alternatively, set fixed passwords in your `values.yaml` for stable local testing.

## Versioning Strategy

Chart version tracks Helm lifecycle (e.g. `0.1.0-rc1`)

appVersion tracks DAMAP version (e.g. `5.0.0-rc1`)

Prereleases are explicitly marked in Artifact Hub