# Workflows

This folder contains 3 Github Actions workflows. All jobs are performed in parallel on a
separate Github runner and each job contains steps which are done sequentially.

- `ci.yml` spins up the containers and runs CI checks such as a DB health check and Maven 
tests.

- `release.yml` automates the deployment of the backend to Github Packages while another job
builds and then publishes the Docker image to the Docker registry. 

- `release-maven.yml` automates the deployment of the backend to the Maven Central repository.

## `ci.yml`
This workflow is automatically triggered upon each Pull Request, each push to `next` and can
also be triggered manually [here](https://github.com/damap-org/damap-backend/actions/workflows/ci.yml).

The `matrix` strategy is used which allows us to run each job more than once with different
configurations, for instance for running the CI checks on a Postgres DB as well as on an 
Oracle one.

Two jobs are defined: `test-docker` builds the Docker image, starts the DAMAP containers and
sees if the application is responding with an `HTTP ok`. The second job, `test-maven`, runs a
DB healthcheck using Oracle, h2 and Postgres, then runs the DAMAP Maven tests.

## `release.yml` 
`release.yml` is automatically triggered when a branch is pushed with a version tag, when 
someone pushes to `next` or `master`, or manually [here](
https://github.com/damap-org/damap-backend/actions/workflows/release.yml
).

The job github-deploy `github-deploy` first sets up JDK17 and automatically configures the
GPG secret key and passphrase retrieved from Github secrets and then signs and publishes the
`jar` artefacts to Github Packages. The other job, `container-build-publish` is responsible for 
building and pushing the Docker image, using the Github token defined as a Github secret.


## `release-meaven.yml`
This workflow is triggered when changes are pushed with a `version` tag or manually [here](
https://github.com/damap-org/damap-backend/actions/workflows/release-maven.yml
). In the case of a manual trigger, an input is required from the user to indicate
the version that is being published.

The single job `maven-deploy` is responsible for setting up JDK17 similarly to `release.yml` 
but this time with a configuration for Maven Central. As before, the artifacts are automatically
signed by a GPG agent by the maven-gpg-plugin, and then published on Maven Central using
the Central credentials retrieved from the Github secrets.

For context, our partner institutions currently use the Maven releases for deployment.




