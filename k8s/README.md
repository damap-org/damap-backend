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

