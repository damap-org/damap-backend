#!/bin/bash
## This script brings up a local KinD cluster suitable for deploying a DAMAP test instance.
## It is loosely based on the instructions here: https://kind.sigs.k8s.io/docs/user/local-registry/
##
## Steps taken:
## - Deploy a Docker registry running at localhost:5000
## - Deploy KinD, mapping out port 80 and port 443 to the host
## - Configure Containerd on each KinD node to use the local registry without encryption
## - Deploy the nginx ingress controller

set -o pipefail

LOGFILE=/tmp/$$

group() {
  if [ -n "${GITHUB_RUN_ID}" ]; then
    echo -n "::group::"
  fi
  if [ "$2" -eq 0 ]; then
    echo -e "\e[32m$1\e[0m"
  else
    echo -e "\e[31m$1\e[0m"
  fi
  cat $LOGFILE
  if [ -n "${GITHUB_RUN_ID}" ]; then
    echo "::endgroup::"
  fi
  if [ "$2" -ne 0 ]; then
    exit "$2"
  fi
}

REGISTRY_CONTAINER_NAME="registry"
REGISTRY_PORT=5000
REGISTRY_DIR="/etc/containerd/certs.d/localhost:${REGISTRY_PORT}"

(
  set -euo pipefail
  docker run \
    -d --restart=always \
    -p "127.0.0.1:${REGISTRY_PORT}:5000" \
    --network bridge \
    --name "${REGISTRY_CONTAINER_NAME}" \
    registry:2
) >$LOGFILE 2>&1
group "🫙 Deploy registry" $?

(
  set -euo pipefail
  cat <<EOF | kind create cluster --wait 300s --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 80
        hostPort: 80
        protocol: TCP
      - containerPort: 443
        hostPort: 443
        protocol: TCP
containerdConfigPatches:
  - |-
    [plugins."io.containerd.grpc.v1.cri".registry]
      config_path = "/etc/containerd/certs.d"
EOF
) >$LOGFILE 2>&1
group "🚀 Start KinD cluster" $?

(
  set -euo pipefail
  for NODE in $(kind get nodes); do
      docker exec "${NODE}" mkdir -p "${REGISTRY_DIR}"
      echo "[host.\"http://registry:${REGISTRY_PORT}\"]
plain_text = true" | docker exec -i "${NODE}" cp /dev/stdin "${REGISTRY_DIR}/hosts.toml"
    done
    if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${REGISTRY_CONTAINER_NAME}")" = 'null' ]; then
      docker network connect "kind" "${REGISTRY_CONTAINER_NAME}"
    fi
    cat <<EOF | kubectl apply -f -
  apiVersion: v1
  kind: ConfigMap
  metadata:
    name: local-registry-hosting
    namespace: kube-public
  data:
    localRegistryHosting.v1: |
      host: "localhost:${REGISTRY_PORT}"
      help: "https://kind.sigs.k8s.io/docs/user/local-registry/"
EOF
) >$LOGFILE 2>&1
group "⚙️ Configure KinD cluster for registry" $?

(
  set -euo pipefail
  kubectl apply -f https://kind.sigs.k8s.io/examples/ingress/deploy-ingress-nginx.yaml
  kubectl wait --namespace ingress-nginx \
    --for=condition=ready pod \
    --selector=app.kubernetes.io/component=controller \
    --timeout=300s
) > /tmp/$$ 2>&1
group "🛂 Deploy ingress controller" $?
