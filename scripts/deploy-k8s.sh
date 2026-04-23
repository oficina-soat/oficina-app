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
REQUIRE_K8S_DB_SECRET="${REQUIRE_K8S_DB_SECRET:-true}"
K8S_JWT_SECRET_NAME="${K8S_JWT_SECRET_NAME:-oficina-jwt-keys}"
JWT_SECRET_SOURCE="${JWT_SECRET_SOURCE:-aws-secrets-manager}"
JWT_SECRET_NAME="${JWT_SECRET_NAME:-oficina/lab/jwt}"
JWT_SECRET_PRIVATE_KEY_FIELD="${JWT_SECRET_PRIVATE_KEY_FIELD:-privateKeyPem}"
JWT_SECRET_PUBLIC_KEY_FIELD="${JWT_SECRET_PUBLIC_KEY_FIELD:-publicKeyPem}"
ROTATE_JWT_SECRET="${ROTATE_JWT_SECRET:-false}"
JWT_DIR="${JWT_DIR:-.tmp/jwt}"
REGENERATE_JWT="${REGENERATE_JWT:-false}"
OFICINA_AUTH_ISSUER="${OFICINA_AUTH_ISSUER:-oficina-api}"
OFICINA_AUTH_JWKS_URI="${OFICINA_AUTH_JWKS_URI:-file:/jwt/publicKey.pem}"

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

dump_rollout_diagnostics() {
  local selector="app.kubernetes.io/name=${K8S_DEPLOYMENT_NAME}"

  log "Diagnostico do rollout"
  kubectl get deployment "${K8S_DEPLOYMENT_NAME}" --namespace "${K8S_NAMESPACE}" -o wide || true
  kubectl get pods --namespace "${K8S_NAMESPACE}" --selector "${selector}" -o wide || true

  log "Describe dos pods da aplicacao"
  kubectl describe pods --namespace "${K8S_NAMESPACE}" --selector "${selector}" || true

  log "Logs atuais do container ${K8S_CONTAINER_NAME}"
  kubectl logs --namespace "${K8S_NAMESPACE}" --selector "${selector}" --container "${K8S_CONTAINER_NAME}" --tail=200 || true

  log "Logs anteriores do container ${K8S_CONTAINER_NAME}"
  kubectl logs --namespace "${K8S_NAMESPACE}" --selector "${selector}" --container "${K8S_CONTAINER_NAME}" --previous --tail=200 || true
}

ensure_db_secret() {
  if secret_exists "${K8S_DB_SECRET_NAME}"; then
    log "Usando secret ${K8S_NAMESPACE}/${K8S_DB_SECRET_NAME}"
    return
  fi

  if [[ "${REQUIRE_K8S_DB_SECRET}" == "true" ]]; then
    cat >&2 <<EOF
Secret obrigatorio ${K8S_NAMESPACE}/${K8S_DB_SECRET_NAME} nao encontrado.

Crie o secret pelo repo oficina-infra-db antes de publicar o app:
  Actions -> Deploy Lab -> Run workflow com APPLY_K8S_SECRET=true

Ou execute manualmente no repo oficina-infra-db:
  DB_SECRET_ARN=<secret-da-aplicacao> \\
  EKS_CLUSTER_NAME=${EKS_CLUSTER_NAME} \\
  UPDATE_KUBECONFIG=true \\
  ./scripts/apply-k8s-secret.sh

Para permitir deploy sem banco, configure REQUIRE_K8S_DB_SECRET=false.
EOF
    exit 1
  fi

  log "Secret opcional ${K8S_NAMESPACE}/${K8S_DB_SECRET_NAME} ausente; seguindo sem variaveis de banco."
}

escape_sed_replacement() {
  printf '%s' "$1" | sed -e 's/[&|\\]/\\&/g'
}

render_overlay() {
  local escaped_image_ref
  local escaped_auth_issuer
  local escaped_auth_jwks_uri
  escaped_image_ref="$(escape_sed_replacement "${IMAGE_REF}")"
  escaped_auth_issuer="$(escape_sed_replacement "${OFICINA_AUTH_ISSUER}")"
  escaped_auth_jwks_uri="$(escape_sed_replacement "${OFICINA_AUTH_JWKS_URI}")"
  kubectl kustomize "${K8S_APP_OVERLAY}" |
    sed "s|IMAGE_PLACEHOLDER|${escaped_image_ref}|g" |
    sed "s|OFICINA_AUTH_ISSUER_PLACEHOLDER|${escaped_auth_issuer}|g" |
    sed "s|OFICINA_AUTH_JWKS_URI_PLACEHOLDER|${escaped_auth_jwks_uri}|g"
}

current_deployment_image() {
  kubectl get deployment "${K8S_DEPLOYMENT_NAME}" \
    --namespace "${K8S_NAMESPACE}" \
    -o jsonpath="{.spec.template.spec.containers[?(@.name=='${K8S_CONTAINER_NAME}')].image}" 2>/dev/null || true
}

generate_jwt_keypair() {
  local jwt_dir="$1"

  require_cmd openssl
  mkdir -p "${jwt_dir}"
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "${jwt_dir}/privateKey.pem"
  openssl pkey -in "${jwt_dir}/privateKey.pem" -pubout -out "${jwt_dir}/publicKey.pem"
  chmod 600 "${jwt_dir}/privateKey.pem"
  chmod 644 "${jwt_dir}/publicKey.pem"
}

aws_jwt_secret_exists() {
  local error_file

  error_file="$(mktemp)"
  if aws --region "${AWS_REGION}" secretsmanager describe-secret \
    --secret-id "${JWT_SECRET_NAME}" >/dev/null 2>"${error_file}"; then
    rm -f "${error_file}"
    return 0
  fi

  if grep -q "ResourceNotFoundException" "${error_file}"; then
    rm -f "${error_file}"
    return 1
  fi

  cat "${error_file}" >&2
  rm -f "${error_file}"
  exit 1
}

create_or_rotate_aws_jwt_secret() {
  local tmp_dir
  local secret_json_file

  require_cmd aws
  require_cmd jq
  require_non_empty "${JWT_SECRET_NAME}" "JWT_SECRET_NAME"
  require_non_empty "${JWT_SECRET_PRIVATE_KEY_FIELD}" "JWT_SECRET_PRIVATE_KEY_FIELD"
  require_non_empty "${JWT_SECRET_PUBLIC_KEY_FIELD}" "JWT_SECRET_PUBLIC_KEY_FIELD"

  tmp_dir="$(mktemp -d)"
  secret_json_file="${tmp_dir}/jwt-secret.json"

  generate_jwt_keypair "${tmp_dir}"

  jq -n \
    --rawfile privateKeyPem "${tmp_dir}/privateKey.pem" \
    --rawfile publicKeyPem "${tmp_dir}/publicKey.pem" \
    --arg privateKeyField "${JWT_SECRET_PRIVATE_KEY_FIELD}" \
    --arg publicKeyField "${JWT_SECRET_PUBLIC_KEY_FIELD}" \
    '{($privateKeyField): $privateKeyPem, ($publicKeyField): $publicKeyPem}' \
    > "${secret_json_file}"

  if aws_jwt_secret_exists; then
    log "Rotacionando secret JWT no AWS Secrets Manager: ${JWT_SECRET_NAME}"
    aws --region "${AWS_REGION}" secretsmanager put-secret-value \
      --secret-id "${JWT_SECRET_NAME}" \
      --secret-string "file://${secret_json_file}" >/dev/null
  else
    log "Criando secret JWT no AWS Secrets Manager: ${JWT_SECRET_NAME}"
    aws --region "${AWS_REGION}" secretsmanager create-secret \
      --name "${JWT_SECRET_NAME}" \
      --description "Chaves JWT compartilhadas da Oficina no ambiente lab" \
      --secret-string "file://${secret_json_file}" >/dev/null
  fi

  rm -rf "${tmp_dir}"
}

ensure_aws_jwt_secret() {
  require_cmd aws
  require_non_empty "${JWT_SECRET_NAME}" "JWT_SECRET_NAME"

  if [[ "${ROTATE_JWT_SECRET}" == "true" ]]; then
    create_or_rotate_aws_jwt_secret
    return
  fi

  if aws_jwt_secret_exists; then
    log "Usando secret JWT existente no AWS Secrets Manager: ${JWT_SECRET_NAME}"
    return
  fi

  create_or_rotate_aws_jwt_secret
}

apply_k8s_jwt_secret_from_aws() {
  local tmp_dir
  local secret_json

  require_cmd aws
  require_cmd jq
  require_non_empty "${JWT_SECRET_NAME}" "JWT_SECRET_NAME"
  require_non_empty "${JWT_SECRET_PRIVATE_KEY_FIELD}" "JWT_SECRET_PRIVATE_KEY_FIELD"
  require_non_empty "${JWT_SECRET_PUBLIC_KEY_FIELD}" "JWT_SECRET_PUBLIC_KEY_FIELD"

  tmp_dir="$(mktemp -d)"
  secret_json="$(
    aws --region "${AWS_REGION}" secretsmanager get-secret-value \
      --secret-id "${JWT_SECRET_NAME}" \
      --query SecretString \
      --output text
  )"

  jq -er --arg field "${JWT_SECRET_PRIVATE_KEY_FIELD}" '.[$field]' <<<"${secret_json}" > "${tmp_dir}/privateKey.pem"
  jq -er --arg field "${JWT_SECRET_PUBLIC_KEY_FIELD}" '.[$field]' <<<"${secret_json}" > "${tmp_dir}/publicKey.pem"

  if ! grep -q "BEGIN PRIVATE KEY" "${tmp_dir}/privateKey.pem"; then
    echo "Campo ${JWT_SECRET_PRIVATE_KEY_FIELD} do secret ${JWT_SECRET_NAME} nao contem uma chave privada PEM valida." >&2
    exit 1
  fi

  if ! grep -q "BEGIN PUBLIC KEY" "${tmp_dir}/publicKey.pem"; then
    echo "Campo ${JWT_SECRET_PUBLIC_KEY_FIELD} do secret ${JWT_SECRET_NAME} nao contem uma chave publica PEM valida." >&2
    exit 1
  fi

  log "Aplicando secret ${K8S_NAMESPACE}/${K8S_JWT_SECRET_NAME} a partir do AWS Secrets Manager"
  kubectl create secret generic "${K8S_JWT_SECRET_NAME}" \
    --from-file=privateKey.pem="${tmp_dir}/privateKey.pem" \
    --from-file=publicKey.pem="${tmp_dir}/publicKey.pem" \
    --namespace "${K8S_NAMESPACE}" \
    --dry-run=client -o yaml | kubectl apply -f -

  rm -rf "${tmp_dir}"
}

apply_k8s_jwt_secret_from_local_files() {
  if [[ "${REGENERATE_JWT}" == "true" || ! -f "${JWT_DIR}/privateKey.pem" || ! -f "${JWT_DIR}/publicKey.pem" ]]; then
    require_cmd openssl
    log "Gerando par de chaves JWT em ${JWT_DIR}"
    generate_jwt_keypair "${JWT_DIR}"
  fi

  if [[ ! -f "${JWT_DIR}/privateKey.pem" || ! -f "${JWT_DIR}/publicKey.pem" ]]; then
    echo "Arquivos JWT nao encontrados em ${JWT_DIR}. Ajuste REGENERATE_JWT=true ou forneca as chaves." >&2
    exit 1
  fi

  log "Aplicando secret ${K8S_NAMESPACE}/${K8S_JWT_SECRET_NAME} a partir de arquivos locais"
  kubectl create secret generic "${K8S_JWT_SECRET_NAME}" \
    --from-file=privateKey.pem="${JWT_DIR}/privateKey.pem" \
    --from-file=publicKey.pem="${JWT_DIR}/publicKey.pem" \
    --namespace "${K8S_NAMESPACE}" \
    --dry-run=client -o yaml | kubectl apply -f -
}

ensure_jwt_secret() {
  case "${JWT_SECRET_SOURCE}" in
    aws-secrets-manager)
      ensure_aws_jwt_secret
      apply_k8s_jwt_secret_from_aws
      ;;
    local-files)
      apply_k8s_jwt_secret_from_local_files
      ;;
    *)
      echo "JWT_SECRET_SOURCE invalido: ${JWT_SECRET_SOURCE}. Use aws-secrets-manager ou local-files." >&2
      exit 1
      ;;
  esac
}

bootstrap_k8s_app() {
  require_cmd sed
  require_non_empty "${K8S_APP_OVERLAY}" "K8S_APP_OVERLAY"

  if [[ "${K8S_NAMESPACE}" != "default" ]]; then
    echo "Bootstrap automatico suporta somente K8S_NAMESPACE=default porque o overlay lab segue o padrao dos repos de infra." >&2
    exit 1
  fi

  ensure_db_secret
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
  ensure_db_secret
  ensure_jwt_secret
  current_image="$(current_deployment_image)"
  kubectl set image \
    "deployment/${K8S_DEPLOYMENT_NAME}" \
    "${K8S_CONTAINER_NAME}=${IMAGE_REF}" \
    --namespace "${K8S_NAMESPACE}"

  if [[ "${current_image}" == "${IMAGE_REF}" ]]; then
    log "Imagem ja estava em ${IMAGE_REF}; reiniciando rollout para recarregar secrets/configs"
    kubectl rollout restart "deployment/${K8S_DEPLOYMENT_NAME}" --namespace "${K8S_NAMESPACE}"
  fi
fi

if ! kubectl rollout status \
  "deployment/${K8S_DEPLOYMENT_NAME}" \
  --namespace "${K8S_NAMESPACE}" \
  --timeout "${K8S_ROLLOUT_TIMEOUT}"; then
  dump_rollout_diagnostics
  exit 1
fi

kubectl get pods \
  --namespace "${K8S_NAMESPACE}" \
  --selector "app.kubernetes.io/name=${K8S_DEPLOYMENT_NAME}"

echo "Deploy concluido com imagem ${IMAGE_REF}"
