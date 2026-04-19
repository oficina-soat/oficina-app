#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-us-east-1}"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-eks-lab}"
IMAGE_REF="${IMAGE_REF:-}"
K8S_NAMESPACE="${K8S_NAMESPACE:-default}"
K8S_DEPLOYMENT_NAME="${K8S_DEPLOYMENT_NAME:-oficina-app}"
K8S_CONTAINER_NAME="${K8S_CONTAINER_NAME:-oficina-app}"
K8S_ROLLOUT_TIMEOUT="${K8S_ROLLOUT_TIMEOUT:-300s}"
UPDATE_KUBECONFIG="${UPDATE_KUBECONFIG:-true}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Comando obrigatorio nao encontrado: $1" >&2
    exit 1
  fi
}

require_non_empty() {
  local value="$1"
  local name="$2"

  if [[ -z "${value}" ]]; then
    echo "Variavel obrigatoria ausente: ${name}" >&2
    exit 1
  fi
}

require_cmd kubectl
require_non_empty "${IMAGE_REF}" "IMAGE_REF"
require_non_empty "${AWS_REGION}" "AWS_REGION"
require_non_empty "${EKS_CLUSTER_NAME}" "EKS_CLUSTER_NAME"

if [[ "${UPDATE_KUBECONFIG}" == "true" ]]; then
  require_cmd aws
  aws eks update-kubeconfig --region "${AWS_REGION}" --name "${EKS_CLUSTER_NAME}"
fi

if ! kubectl get deployment "${K8S_DEPLOYMENT_NAME}" --namespace "${K8S_NAMESPACE}" >/dev/null 2>&1; then
  echo "Deployment ${K8S_NAMESPACE}/${K8S_DEPLOYMENT_NAME} nao encontrado." >&2
  echo "Execute o deploy inicial dos manifests pelo repo oficina-infra-k8s antes de publicar a imagem do app." >&2
  exit 1
fi

kubectl set image \
  "deployment/${K8S_DEPLOYMENT_NAME}" \
  "${K8S_CONTAINER_NAME}=${IMAGE_REF}" \
  --namespace "${K8S_NAMESPACE}"

kubectl rollout status \
  "deployment/${K8S_DEPLOYMENT_NAME}" \
  --namespace "${K8S_NAMESPACE}" \
  --timeout "${K8S_ROLLOUT_TIMEOUT}"

kubectl get pods \
  --namespace "${K8S_NAMESPACE}" \
  --selector "app.kubernetes.io/name=${K8S_DEPLOYMENT_NAME}"

echo "Deploy concluido com imagem ${IMAGE_REF}"
