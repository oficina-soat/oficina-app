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
BOOTSTRAP_K8S_APP_IF_MISSING="${BOOTSTRAP_K8S_APP_IF_MISSING:-true}"
K8S_APP_OVERLAY="${K8S_APP_OVERLAY:-k8s/overlays/lab}"
K8S_DB_SECRET_NAME="${K8S_DB_SECRET_NAME:-oficina-database-env}"
JWT_DIR="${JWT_DIR:-.tmp/jwt}"
REGENERATE_JWT="${REGENERATE_JWT:-true}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

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

log() {
  printf '\n[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

secret_exists() {
  local secret_name="$1"

  kubectl get secret "${secret_name}" --namespace "${K8S_NAMESPACE}" >/dev/null 2>&1
}

escape_sed_replacement() {
  printf '%s' "$1" | sed -e 's/[&|\\]/\\&/g'
}

render_overlay() {
  local escaped_image_ref
  escaped_image_ref="$(escape_sed_replacement "${IMAGE_REF}")"
  kubectl kustomize "${K8S_APP_OVERLAY}" | sed "s|IMAGE_PLACEHOLDER|${escaped_image_ref}|g"
}

ensure_jwt_secret() {
  if [[ "${REGENERATE_JWT}" == "true" || ! -f "${JWT_DIR}/privateKey.pem" || ! -f "${JWT_DIR}/publicKey.pem" ]]; then
    require_cmd openssl
    log "Gerando par de chaves JWT em ${JWT_DIR}"
    mkdir -p "${JWT_DIR}"
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "${JWT_DIR}/privateKey.pem"
    openssl pkey -in "${JWT_DIR}/privateKey.pem" -pubout -out "${JWT_DIR}/publicKey.pem"
  fi

  if [[ ! -f "${JWT_DIR}/privateKey.pem" || ! -f "${JWT_DIR}/publicKey.pem" ]]; then
    echo "Arquivos JWT nao encontrados em ${JWT_DIR}. Ajuste REGENERATE_JWT=true ou forneca as chaves." >&2
    exit 1
  fi

  log "Aplicando secret ${K8S_NAMESPACE}/oficina-jwt-keys"
  kubectl create secret generic oficina-jwt-keys \
    --from-file=privateKey.pem="${JWT_DIR}/privateKey.pem" \
    --from-file=publicKey.pem="${JWT_DIR}/publicKey.pem" \
    --namespace "${K8S_NAMESPACE}" \
    --dry-run=client -o yaml | kubectl apply -f -
}

bootstrap_k8s_app() {
  require_cmd sed
  require_non_empty "${K8S_APP_OVERLAY}" "K8S_APP_OVERLAY"

  if [[ "${K8S_NAMESPACE}" != "default" ]]; then
    echo "Bootstrap automatico suporta somente K8S_NAMESPACE=default porque o overlay lab segue o padrao dos repos de infra." >&2
    exit 1
  fi

  if secret_exists "${K8S_DB_SECRET_NAME}"; then
    log "Usando secret opcional ${K8S_NAMESPACE}/${K8S_DB_SECRET_NAME}"
  else
    log "Secret opcional ${K8S_NAMESPACE}/${K8S_DB_SECRET_NAME} ausente; seguindo sem variaveis de banco."
  fi

  ensure_jwt_secret

  log "Aplicando manifests iniciais da aplicacao"
  render_overlay | kubectl apply -f -
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
  if [[ "${BOOTSTRAP_K8S_APP_IF_MISSING}" != "true" ]]; then
    echo "Deployment ${K8S_NAMESPACE}/${K8S_DEPLOYMENT_NAME} nao encontrado." >&2
    echo "Habilite BOOTSTRAP_K8S_APP_IF_MISSING=true ou aplique os manifests iniciais antes do deploy." >&2
    exit 1
  fi

  log "Deployment ${K8S_NAMESPACE}/${K8S_DEPLOYMENT_NAME} nao encontrado; executando bootstrap inicial"
  bootstrap_k8s_app
else
  kubectl set image \
    "deployment/${K8S_DEPLOYMENT_NAME}" \
    "${K8S_CONTAINER_NAME}=${IMAGE_REF}" \
    --namespace "${K8S_NAMESPACE}"
fi

kubectl rollout status \
  "deployment/${K8S_DEPLOYMENT_NAME}" \
  --namespace "${K8S_NAMESPACE}" \
  --timeout "${K8S_ROLLOUT_TIMEOUT}"

kubectl get pods \
  --namespace "${K8S_NAMESPACE}" \
  --selector "app.kubernetes.io/name=${K8S_DEPLOYMENT_NAME}"

echo "Deploy concluido com imagem ${IMAGE_REF}"
