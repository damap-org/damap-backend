# PURE Integration Environment Variables

This document shows how to configure DAMAP PURE integration using environment variables, following the [official DAMAP documentation](https://damap.org/manual/cris/pure/).

## Environment Variables

| Environment Variable | Default Value | Description |
|---------------------|---------------|-------------|
| `DAMAP_ELSEVIER_PURE_BACKEND` | `http` | Backend type: `http` or `file` |
| `DAMAP_ELSEVIER_PURE_ENDPOINT_URL` | `https://tugraz-staging.elsevierpure.com/ws/api` | HTTP endpoint URL |
| `DAMAP_ELSEVIER_PURE_API_KEY` | `your-pure-api-key-here` | API key for HTTP backend |
| `DAMAP_ELSEVIER_PURE_DESCRIPTION_CLASSIFICATION` | `/dk/atira/pure/projectdescription` | Project description field |
| `DAMAP_ELSEVIER_PURE_PROJECT_LEAD_ROLE_CLASSIFICATION` | `/dk/atira/pure/projectlead` | Project lead role |
| `DAMAP_ELSEVIER_PURE_CONTRIBUTOR_ROLE_CLASSIFICATIONS` | See below | JSON mapping of roles |

## Role Classifications

```yaml
/dk/atira/pure/member: PROJECT_MEMBER
/dk/atira/pure/projectlead: PROJECT_LEADER  
/dk/atira/pure/upmproject/roles/upmproject/coi: PROJECT_MEMBER
```

## Example Environment Setup

```bash
# TU Graz staging environment
export DAMAP_ELSEVIER_PURE_BACKEND=http
export DAMAP_ELSEVIER_PURE_ENDPOINT_URL=https://tugraz-staging.elsevierpure.com/ws/api
export DAMAP_ELSEVIER_PURE_API_KEY=your-pure-api-key-here

# Role mappings (discovered from real TU Graz data)
export DAMAP_ELSEVIER_PURE_CONTRIBUTOR_ROLE_CLASSIFICATIONS='{"\/dk\/atira\/pure\/member": "PROJECT_MEMBER", "\/dk\/atira\/pure\/projectlead": "PROJECT_LEADER", "\/dk\/atira\/pure\/upmproject\/roles\/upmproject\/coi": "PROJECT_MEMBER"}'
```

## Docker Usage

```bash
# Using environment variables with Docker
docker run \
  -e DAMAP_ELSEVIER_PURE_BACKEND=http \
  -e DAMAP_ELSEVIER_PURE_ENDPOINT_URL=https://your-pure-instance.com/ws/api \
  -e DAMAP_ELSEVIER_PURE_API_KEY=your-api-key \
  damap:latest
```

## Testing Role Mappings

Our test suite validates that role mappings work correctly with real PURE data:

```bash
PURE_INTEGRATION_TEST=true mvn test -Dtest=PureServicesIntegrationTest#testProjectPersonsExtraction
```

TU Graz uses the role classification:
- `/dk/atira/pure/upmproject/roles/upmproject/coi` for project participants 