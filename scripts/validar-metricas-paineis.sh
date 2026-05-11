#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="${ROOT_DIR}/.tmp/validar-metricas-paineis"
PF_DIR="${ROOT_DIR}/.tmp/port-forward"

APP_NAMESPACE="${APP_NAMESPACE:-default}"
APP_SERVICE="${APP_SERVICE:-oficina-app}"
AUTH_CONFIGMAP="${AUTH_CONFIGMAP:-oficina-app-config}"
K8S_JWT_SECRET="${K8S_JWT_SECRET:-oficina-jwt-keys}"
APP_LOCAL_PORT="${APP_LOCAL_PORT:-8080}"
APP_SERVICE_PORT="${APP_SERVICE_PORT:-8080}"
ENABLE_PORT_FORWARD="${ENABLE_PORT_FORWARD:-true}"
STOP_PORT_FORWARD_ON_EXIT="${STOP_PORT_FORWARD_ON_EXIT:-false}"
AWS_REGION="${AWS_REGION:-us-east-1}"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-eks-lab}"
API_GATEWAY_ID="${API_GATEWAY_ID:-}"
API_GATEWAY_NAME="${API_GATEWAY_NAME:-${EKS_CLUSTER_NAME}-http-api}"
UPDATE_KUBECONFIG="${UPDATE_KUBECONFIG:-auto}"

APP_BASE_URL_INFORMED="${APP_BASE_URL+x}"
APP_BASE_URL="${APP_BASE_URL:-http://localhost:${APP_LOCAL_PORT}}"
MODO_ACESSO="${MODO_ACESSO:-${ACCESS_MODE:-}}"
AUTH_MODE="${AUTH_MODE:-auto}"
AUTH_BASE_URL="${OFICINA_AUTH_BASE_URL:-${AUTH_BASE_URL:-}}"
AUTH_PASSWORD="${AUTH_PASSWORD:-secret}"
TOKEN_ISSUER=""
TOKEN_JWT_DIR=""
AUTH_ADMIN_CPF="${AUTH_ADMIN_CPF:-84191404067}"
AUTH_MECANICO_CPF="${AUTH_MECANICO_CPF:-36655462007}"
AUTH_RECEPCIONISTA_CPF="${AUTH_RECEPCIONISTA_CPF:-17245011010}"
SKIP_MAGIC_LINK="${SKIP_MAGIC_LINK:-false}"
FORCAR_FALHAS_INTEGRACAO="${FORCAR_FALHAS_INTEGRACAO:-true}"
FORCAR_FALHAS_OS="${FORCAR_FALHAS_OS:-true}"
EXECUCOES="${EXECUCOES:-${RUNS:-1}}"
RUN_FOREVER="${RUN_FOREVER:-false}"
PAUSE_SECONDS="${PAUSE_SECONDS:-0}"
CURL_RETRIES="${CURL_RETRIES:-3}"
CURL_RETRY_DELAY="${CURL_RETRY_DELAY:-2}"
CURL_CONNECT_TIMEOUT="${CURL_CONNECT_TIMEOUT:-10}"
CURL_MAX_TIME="${CURL_MAX_TIME:-60}"

RUN_SEED_BASE="${RUN_SEED:-}"
RUN_LABEL_BASE="${RUN_LABEL:-metrics}"
RUN_SEED=""
RUN_LABEL=""

ADMIN_TOKEN=""
MECANICO_TOKEN=""
RECEPCIONISTA_TOKEN=""
LAST_BODY=""
LAST_STATUS=""
LAST_HTTP_OK="false"
ITERATION_ERRORS=0
TOTAL_ERRORS=0
ERRORS_FILE="${TMP_DIR}/errors.$$"
ITERATION_ERRORS_FILE=""
STARTED_PF_PID=""

usage() {
  cat <<EOF
Uso:
  ./scripts/validar-metricas-paineis.sh

Objetivo:
  Gera trafego HTTP para validar APIs, ciclo de vida da OS, metricas Prometheus
  e dashboards que usam as metricas/logs do laboratorio.
  Respostas inesperadas sao contabilizadas e a execucao continua ate completar
  os ciclos configurados ou ate Ctrl+C.

Variaveis principais:
  MODO_ACESSO              encaminhamento|aws. Default: encaminhamento
  APP_BASE_URL              Base da aplicacao. Default: http://localhost:8080
  ENABLE_PORT_FORWARD       Compatibilidade: false equivale a MODO_ACESSO=aws
  APP_NAMESPACE             Namespace Kubernetes. Default: default
  APP_SERVICE               Service Kubernetes. Default: oficina-app
  AUTH_CONFIGMAP            ConfigMap com OFICINA_AUTH_ISSUER. Default: oficina-app-config
  K8S_JWT_SECRET            Secret Kubernetes com chaves JWT. Default: oficina-jwt-keys
  APP_LOCAL_PORT            Porta local do port-forward. Default: 8080
  UPDATE_KUBECONFIG         auto|true|false. Default: auto
  EKS_CLUSTER_NAME          Nome do cluster EKS. Default: eks-lab
  API_GATEWAY_ID            ID do HTTP API Gateway. Opcional
  API_GATEWAY_NAME          Nome do HTTP API Gateway. Default: eks-lab-http-api
  AWS_REGION                Regiao AWS. Default: us-east-1
  AUTH_MODE                 auto|auth-api|dev-jwt. Default: auto
  OFICINA_AUTH_BASE_URL     Base do auth/API Gateway para POST /auth/token
  AUTH_PASSWORD             Senha dos usuarios seed do lab. Default: secret
  SKIP_MAGIC_LINK           true|false. Default: false
  FORCAR_FALHAS_INTEGRACAO  true|false. Gera falha esperada na notificacao. Default: true
  FORCAR_FALHAS_OS          true|false. Gera falhas esperadas no processamento da OS. Default: true
  EXECUCOES                 Quantidade de execucoes. Default: 1
  RUNS                      Alias de EXECUCOES
  RUN_FOREVER               true|false. Quando true, roda ate Ctrl+C. Default: false
  PAUSE_SECONDS             Pausa entre execucoes. Default: 0
  CURL_RETRIES              Novas tentativas para falhas de rede. Default: 3
  CURL_RETRY_DELAY          Pausa entre novas tentativas HTTP. Default: 2
  STOP_PORT_FORWARD_ON_EXIT true|false. Default: false

Exemplos:
  ./scripts/validar-metricas-paineis.sh
  MODO_ACESSO=aws ./scripts/validar-metricas-paineis.sh
  EXECUCOES=5 ./scripts/validar-metricas-paineis.sh
  MODO_ACESSO=aws RUN_FOREVER=true PAUSE_SECONDS=10 ./scripts/validar-metricas-paineis.sh
EOF
}

log() {
  printf '[validar] %s\n' "$*" >&2
}

fail() {
  printf '[validar][erro] %s\n' "$*" >&2
  exit 1
}

record_validation_error() {
  ITERATION_ERRORS=$((ITERATION_ERRORS + 1))
  TOTAL_ERRORS=$((TOTAL_ERRORS + 1))
  printf '[validar][erro-contabilizado] %s\n' "$*" >&2
  mkdir -p "${TMP_DIR}"
  printf '%s\n' "$*" >> "${ERRORS_FILE}"
  if [[ -n "${ITERATION_ERRORS_FILE}" ]]; then
    printf '%s\n' "$*" >> "${ITERATION_ERRORS_FILE}"
  fi
}

count_file_lines() {
  local file="$1"
  if [[ -f "${file}" ]]; then
    wc -l < "${file}" | tr -d '[:space:]'
  else
    printf '0'
  fi
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Comando obrigatorio nao encontrado: $1"
}

is_truthy() {
  case "$1" in
    true|TRUE|1|yes|YES|sim|SIM) return 0 ;;
    *) return 1 ;;
  esac
}

validate_loop_config() {
  if ! [[ "${EXECUCOES}" =~ ^[0-9]+$ ]] || [[ "${EXECUCOES}" -lt 1 ]]; then
    fail "EXECUCOES deve ser um inteiro positivo."
  fi

  if ! [[ "${PAUSE_SECONDS}" =~ ^[0-9]+$ ]]; then
    fail "PAUSE_SECONDS deve ser um inteiro maior ou igual a zero."
  fi

  if [[ -n "${RUN_SEED_BASE}" ]] && ! [[ "${RUN_SEED_BASE}" =~ ^[0-9]+$ ]]; then
    fail "RUN_SEED deve conter apenas numeros."
  fi

  if ! [[ "${CURL_RETRIES}" =~ ^[0-9]+$ ]]; then
    fail "CURL_RETRIES deve ser um inteiro maior ou igual a zero."
  fi

  if ! [[ "${CURL_RETRY_DELAY}" =~ ^[0-9]+$ ]]; then
    fail "CURL_RETRY_DELAY deve ser um inteiro maior ou igual a zero."
  fi
}

normalize_access_mode() {
  local mode="$1"
  case "${mode}" in
    ""|encaminhamento|port-forward|port_forward|pf)
      printf 'encaminhamento'
      ;;
    aws|gateway|api-gateway|api_gateway|sem-encaminhamento|sem_encaminhamento|sem_encaminhamento_de_porta)
      printf 'aws'
      ;;
    *)
      fail "MODO_ACESSO deve ser encaminhamento ou aws."
      ;;
  esac
}

resolve_api_gateway_id() {
  if [[ -n "${API_GATEWAY_ID}" ]]; then
    printf '%s' "${API_GATEWAY_ID}"
    return
  fi

  require_cmd aws
  aws --region "${AWS_REGION}" apigatewayv2 get-apis \
    --query "Items[?Name=='${API_GATEWAY_NAME}'].ApiId | [0]" \
    --output text 2>/dev/null | sed '/^None$/d'
}

api_gateway_endpoint() {
  local api_id="$1"
  require_cmd aws
  aws --region "${AWS_REGION}" apigatewayv2 get-api \
    --api-id "${api_id}" \
    --query 'ApiEndpoint' \
    --output text 2>/dev/null | sed '/^None$/d'
}

discover_aws_app_base_url() {
  local api_id
  local endpoint

  api_id="$(resolve_api_gateway_id || true)"
  [[ -n "${api_id}" ]] || fail "Nao foi possivel descobrir o API Gateway. Informe API_GATEWAY_ID ou API_GATEWAY_NAME."

  endpoint="$(api_gateway_endpoint "${api_id}")"
  [[ -n "${endpoint}" ]] || fail "Nao foi possivel consultar o endpoint do API Gateway ${api_id}."

  printf '%s' "${endpoint%/}"
}

configure_access_mode() {
  if [[ -z "${MODO_ACESSO}" && "${ENABLE_PORT_FORWARD}" == "false" ]]; then
    MODO_ACESSO="aws"
  fi

  MODO_ACESSO="$(normalize_access_mode "${MODO_ACESSO}")"

  case "${MODO_ACESSO}" in
    encaminhamento)
      ENABLE_PORT_FORWARD="true"
      if [[ -z "${APP_BASE_URL_INFORMED}" ]]; then
        APP_BASE_URL="http://localhost:${APP_LOCAL_PORT}"
      fi
      ;;
    aws)
      ENABLE_PORT_FORWARD="false"
      if [[ -z "${APP_BASE_URL_INFORMED}" ]]; then
        APP_BASE_URL="$(discover_aws_app_base_url)"
      fi
      if [[ -z "${AUTH_BASE_URL}" ]]; then
        AUTH_BASE_URL="${APP_BASE_URL}"
      fi
      ;;
  esac
}

prepare_iteration_context() {
  local iteration="$1"
  if [[ -n "${RUN_SEED_BASE}" ]]; then
    RUN_SEED="$((10#${RUN_SEED_BASE} + iteration - 1))"
  else
    RUN_SEED="$(date +%s)$((RANDOM + iteration))"
  fi
  RUN_LABEL="${RUN_LABEL_BASE}-${RUN_SEED}"
}

local_port_open() {
  local port="$1"
  bash -c ":</dev/tcp/127.0.0.1/${port}" >/dev/null 2>&1
}

app_http_available() {
  curl -fsS --max-time 3 "${APP_BASE_URL}/q/health/ready" >/dev/null 2>&1 \
    || curl -fsS --max-time 3 "${APP_BASE_URL}/q/health/live" >/dev/null 2>&1
}

current_kube_server() {
  kubectl config view --minify --output=jsonpath='{.clusters[0].cluster.server}' 2>/dev/null || true
}

eks_cluster_endpoint() {
  aws eks describe-cluster \
    --region "${AWS_REGION}" \
    --name "${EKS_CLUSTER_NAME}" \
    --query 'cluster.endpoint' \
    --output text 2>/dev/null || true
}

update_kubeconfig() {
  log "Atualizando kubeconfig do cluster ${EKS_CLUSTER_NAME} em ${AWS_REGION}."
  aws eks update-kubeconfig --region "${AWS_REGION}" --name "${EKS_CLUSTER_NAME}" >/dev/null
}

ensure_kubeconfig() {
  case "${UPDATE_KUBECONFIG}" in
    true)
      require_cmd aws
      update_kubeconfig
      ;;
    auto)
      if ! command -v aws >/dev/null 2>&1; then
        log "AWS CLI nao encontrado; usando kubeconfig atual."
        return 0
      fi

      local current_server expected_endpoint
      current_server="$(current_kube_server)"
      expected_endpoint="$(eks_cluster_endpoint)"

      if [[ -z "${expected_endpoint}" || "${expected_endpoint}" == "None" ]]; then
        log "Nao foi possivel consultar o cluster ${EKS_CLUSTER_NAME}; usando kubeconfig atual."
        return 0
      fi

      if [[ "${current_server}" != "${expected_endpoint}" ]]; then
        update_kubeconfig
      fi
      ;;
    false)
      ;;
    *)
      fail "UPDATE_KUBECONFIG deve ser auto, true ou false."
      ;;
  esac
}

pid_is_port_forward() {
  local pid="$1"
  local command_line
  command_line="$(ps -p "${pid}" -o command= 2>/dev/null || true)"
  [[ "${command_line}" == *"kubectl"* && "${command_line}" == *"port-forward"* && "${command_line}" == *"svc/${APP_SERVICE}"* ]]
}

wait_for_port() {
  local port="$1"
  local pid="$2"
  local log_file="$3"

  for _ in {1..20}; do
    if [[ -n "${pid}" ]] && ! kill -0 "${pid}" >/dev/null 2>&1; then
      tail -40 "${log_file}" >&2 || true
      fail "Falha ao iniciar port-forward para ${APP_NAMESPACE}/${APP_SERVICE}."
    fi
    if local_port_open "${port}"; then
      return 0
    fi
    sleep 1
  done

  tail -40 "${log_file}" >&2 || true
  fail "Porta local ${port} nao ficou acessivel."
}

wait_for_app_http() {
  local pid="${1:-}"
  local log_file="${2:-}"

  for _ in {1..30}; do
    if app_http_available; then
      return 0
    fi
    if [[ -n "${pid}" ]] && ! kill -0 "${pid}" >/dev/null 2>&1; then
      [[ -n "${log_file}" ]] && tail -40 "${log_file}" >&2 || true
      fail "Port-forward encerrou antes da aplicacao responder em ${APP_BASE_URL}."
    fi
    sleep 1
  done

  [[ -n "${log_file}" ]] && tail -40 "${log_file}" >&2 || true
  fail "Aplicacao nao respondeu em ${APP_BASE_URL}."
}

start_port_forward() {
  [[ "${ENABLE_PORT_FORWARD}" == "true" ]] || return 0

  require_cmd kubectl
  mkdir -p "${PF_DIR}"
  ensure_kubeconfig

  local pid_file="${PF_DIR}/${APP_SERVICE}.pid"
  local log_file="${PF_DIR}/${APP_SERVICE}.log"

  if [[ -f "${pid_file}" ]]; then
    local existing_pid
    existing_pid="$(cat "${pid_file}")"
    if kill -0 "${existing_pid}" >/dev/null 2>&1 && pid_is_port_forward "${existing_pid}" && local_port_open "${APP_LOCAL_PORT}"; then
      if app_http_available; then
        log "Port-forward ja ativo para ${APP_NAMESPACE}/${APP_SERVICE} em ${APP_BASE_URL} (pid ${existing_pid})."
        return 0
      fi
      log "Port-forward salvo em ${pid_file} nao responde HTTP; reiniciando."
      kill "${existing_pid}" >/dev/null 2>&1 || true
    fi
    rm -f "${pid_file}"
  fi

  if local_port_open "${APP_LOCAL_PORT}"; then
    if app_http_available; then
      log "Porta local ${APP_LOCAL_PORT} ja esta aberta; usando ${APP_BASE_URL} sem iniciar novo port-forward."
      return 0
    fi
    fail "Porta local ${APP_LOCAL_PORT} esta aberta, mas ${APP_BASE_URL} nao respondeu como a aplicacao."
  fi

  if ! kubectl get svc "${APP_SERVICE}" --namespace "${APP_NAMESPACE}" >/dev/null 2>&1; then
    if app_http_available; then
      log "Service ${APP_NAMESPACE}/${APP_SERVICE} nao encontrado, mas ${APP_BASE_URL} ja responde; seguindo sem port-forward."
      return 0
    fi

    fail "Service ${APP_NAMESPACE}/${APP_SERVICE} nao encontrado ou cluster inacessivel. Para lab, tente UPDATE_KUBECONFIG=true EKS_CLUSTER_NAME=${EKS_CLUSTER_NAME} AWS_REGION=${AWS_REGION} ./scripts/validar-metricas-paineis.sh. Para app local, rode com ENABLE_PORT_FORWARD=false APP_BASE_URL=${APP_BASE_URL}."
  fi

  log "Iniciando port-forward ${APP_NAMESPACE}/${APP_SERVICE} ${APP_LOCAL_PORT}:${APP_SERVICE_PORT}."
  : > "${log_file}"
  if command -v setsid >/dev/null 2>&1; then
    setsid kubectl --namespace "${APP_NAMESPACE}" port-forward "svc/${APP_SERVICE}" "${APP_LOCAL_PORT}:${APP_SERVICE_PORT}" >"${log_file}" 2>&1 &
  else
    nohup kubectl --namespace "${APP_NAMESPACE}" port-forward "svc/${APP_SERVICE}" "${APP_LOCAL_PORT}:${APP_SERVICE_PORT}" >"${log_file}" 2>&1 &
  fi

  STARTED_PF_PID="$!"
  echo "${STARTED_PF_PID}" > "${pid_file}"
  wait_for_port "${APP_LOCAL_PORT}" "${STARTED_PF_PID}" "${log_file}"
  wait_for_app_http "${STARTED_PF_PID}" "${log_file}"
  log "Port-forward ativo em ${APP_BASE_URL}. Logs: ${log_file}"
}

cleanup() {
  if [[ "${STOP_PORT_FORWARD_ON_EXIT}" == "true" && -n "${STARTED_PF_PID}" ]]; then
    kill "${STARTED_PF_PID}" >/dev/null 2>&1 || true
    rm -f "${PF_DIR}/${APP_SERVICE}.pid"
  fi
}

on_interrupt() {
  TOTAL_ERRORS="$(count_file_lines "${ERRORS_FILE}")"
  log "Execucao interrompida. Erros contabilizados ate agora: ${TOTAL_ERRORS}."
  exit 130
}

curl_request() {
  local method="$1"
  local url="$2"
  local expected_statuses="$3"
  local token="${4:-}"
  local body="${5:-}"
  local content_type="${6:-application/json}"
  local response_file="${TMP_DIR}/response.$$"
  local status
  local expected_status
  local status_ok="false"
  local request_id="${RUN_LABEL}-$(date +%s%N)"
  local attempt=1
  local max_attempts=$((CURL_RETRIES + 1))
  local args=(-sS -o "${response_file}" -w "%{http_code}" --connect-timeout "${CURL_CONNECT_TIMEOUT}" --max-time "${CURL_MAX_TIME}" -X "${method}" -H "X-Request-Id: ${request_id}")
  LAST_BODY=""
  LAST_STATUS=""
  LAST_HTTP_OK="false"

  if [[ -n "${token}" ]]; then
    args+=(-H "Authorization: Bearer ${token}")
  fi

  if [[ -n "${body}" ]]; then
    args+=(-H "Content-Type: ${content_type}" --data "${body}")
  fi

  while true; do
    if status="$(curl "${args[@]}" "${url}")"; then
      break
    fi

    if (( attempt >= max_attempts )); then
      LAST_BODY="$(cat "${response_file}" 2>/dev/null || true)"
      rm -f "${response_file}"
      LAST_STATUS="NETWORK"
      record_validation_error "Falha de rede em ${method} ${url} apos ${CURL_RETRIES} nova(s) tentativa(s)."
      return 0
    fi

    log "Falha de rede em ${method} ${url}; nova tentativa ${attempt}/${CURL_RETRIES} em ${CURL_RETRY_DELAY}s."
    attempt=$((attempt + 1))
    sleep "${CURL_RETRY_DELAY}"
  done

  LAST_BODY="$(cat "${response_file}" 2>/dev/null || true)"
  rm -f "${response_file}"
  LAST_STATUS="${status}"

  IFS=',' read -r -a expected_status_array <<<"${expected_statuses}"
  for expected_status in "${expected_status_array[@]}"; do
    expected_status="${expected_status//[[:space:]]/}"
    if [[ "${status}" == "${expected_status}" ]]; then
      status_ok="true"
      break
    fi
  done

  if [[ "${status_ok}" != "true" ]]; then
    printf '%s\n' "${LAST_BODY}" >&2
    record_validation_error "${method} ${url} retornou HTTP ${status}; esperado ${expected_statuses}."
    return 0
  fi

  LAST_HTTP_OK="true"
  log "HTTP ${status} ${method} ${url}"
}

api() {
  local method="$1"
  local path="$2"
  local expected_status="$3"
  local token="${4:-}"
  local body="${5:-}"
  local content_type="${6:-application/json}"
  curl_request "${method}" "${APP_BASE_URL}${path}" "${expected_status}" "${token}" "${body}" "${content_type}"
}

auth_api_token() {
  local cpf="$1"
  local target_var="$2"
  local body
  local token
  body="$(jq -cn --arg cpf "${cpf}" --arg password "${AUTH_PASSWORD}" '{cpf:$cpf,password:$password}')"
  curl_request "POST" "${AUTH_BASE_URL%/}/auth/token" "200" "" "${body}"
  if [[ "${LAST_HTTP_OK}" != "true" ]]; then
    printf -v "${target_var}" ''
    return 0
  fi

  token="$(jq -er '.access_token' <<<"${LAST_BODY}" 2>/dev/null || true)"
  if [[ -z "${token}" ]]; then
    record_validation_error "POST ${AUTH_BASE_URL%/}/auth/token nao retornou access_token para cpf ${cpf}."
  fi
  printf -v "${target_var}" '%s' "${token}"
}

dev_jwt_token() {
  local cpf="$1"
  local roles="$2"
  JWT_DIR="${TOKEN_JWT_DIR:-${JWT_DIR:-}}" \
    OFICINA_AUTH_ISSUER="${TOKEN_ISSUER:-${OFICINA_AUTH_ISSUER:-oficina-api}}" \
    "${ROOT_DIR}/scripts/generate-dev-jwt-token.sh" --subject "${cpf}" --roles "${roles}" --ttl 7200
}

discover_k8s_auth_config() {
  command -v kubectl >/dev/null 2>&1 || return 0
  ensure_kubeconfig

  local issuer
  issuer="$(kubectl get configmap "${AUTH_CONFIGMAP}" --namespace "${APP_NAMESPACE}" \
    -o jsonpath='{.data.OFICINA_AUTH_ISSUER}' 2>/dev/null || true)"

  if [[ -n "${issuer}" ]]; then
    TOKEN_ISSUER="${issuer%/}"
    log "Issuer de autenticacao descoberto no ConfigMap ${APP_NAMESPACE}/${AUTH_CONFIGMAP}: ${TOKEN_ISSUER}."

    if [[ -z "${AUTH_BASE_URL}" && ( "${TOKEN_ISSUER}" == http://* || "${TOKEN_ISSUER}" == https://* ) ]]; then
      AUTH_BASE_URL="${TOKEN_ISSUER}"
      log "AUTH_BASE_URL nao informado; usando issuer descoberto para autenticar via /auth/token."
    fi
  fi

  if [[ -n "${TOKEN_ISSUER}" ]]; then
    local jwt_dir="${TMP_DIR}/k8s-jwt"
    mkdir -p "${jwt_dir}"
    if kubectl get secret "${K8S_JWT_SECRET}" --namespace "${APP_NAMESPACE}" \
      -o jsonpath='{.data.privateKey\.pem}' 2>/dev/null | base64 -d > "${jwt_dir}/privateKey.pem" \
      && grep -q "BEGIN PRIVATE KEY" "${jwt_dir}/privateKey.pem"; then
      TOKEN_JWT_DIR="${jwt_dir}"
      chmod 600 "${jwt_dir}/privateKey.pem"
      log "Chave privada JWT descoberta no Secret ${APP_NAMESPACE}/${K8S_JWT_SECRET}; usando JWT compativel com o ambiente."
    else
      rm -f "${jwt_dir}/privateKey.pem"
    fi
  fi
}

authenticate() {
  discover_k8s_auth_config

  case "${AUTH_MODE}" in
    auto)
      if [[ -n "${TOKEN_JWT_DIR}" ]]; then
        AUTH_MODE="dev-jwt"
        log "AUTH_MODE=auto: usando JWT assinado com a chave do Secret Kubernetes."
      elif [[ -n "${AUTH_BASE_URL}" ]]; then
        AUTH_MODE="auth-api"
      else
        AUTH_MODE="dev-jwt"
      fi
      ;;
    auth-api|dev-jwt)
      ;;
    *)
      fail "AUTH_MODE deve ser auto, auth-api ou dev-jwt."
      ;;
  esac

  if [[ "${AUTH_MODE}" == "auth-api" ]]; then
    [[ -n "${AUTH_BASE_URL}" ]] || fail "AUTH_MODE=auth-api exige OFICINA_AUTH_BASE_URL ou AUTH_BASE_URL."
    log "Autenticando via ${AUTH_BASE_URL%/}/auth/token."
    auth_api_token "${AUTH_ADMIN_CPF}" ADMIN_TOKEN
    auth_api_token "${AUTH_MECANICO_CPF}" MECANICO_TOKEN
    auth_api_token "${AUTH_RECEPCIONISTA_CPF}" RECEPCIONISTA_TOKEN
  else
    log "Gerando JWTs locais de desenvolvimento."
    ADMIN_TOKEN="$(dev_jwt_token "${AUTH_ADMIN_CPF}" "administrativo,mecanico,recepcionista")"
    MECANICO_TOKEN="$(dev_jwt_token "${AUTH_MECANICO_CPF}" "mecanico")"
    RECEPCIONISTA_TOKEN="$(dev_jwt_token "${AUTH_RECEPCIONISTA_CPF}" "recepcionista")"
  fi
}

cpf_from_seed() {
  local seed="$1"
  local base
  local sum=0
  local digit
  local d1
  local d2

  base="$(printf '%09d' "$((10#${seed} % 1000000000))")"
  if [[ "${base}" =~ ^([0-9])\1{8}$ ]]; then
    base="123${base:3:6}"
  fi

  for i in {0..8}; do
    digit="${base:i:1}"
    sum=$((sum + 10#${digit} * (10 - i)))
  done
  d1=$((sum % 11))
  if (( d1 < 2 )); then d1=0; else d1=$((11 - d1)); fi

  sum=0
  for i in {0..8}; do
    digit="${base:i:1}"
    sum=$((sum + 10#${digit} * (11 - i)))
  done
  sum=$((sum + d1 * 2))
  d2=$((sum % 11))
  if (( d2 < 2 )); then d2=0; else d2=$((11 - d2)); fi

  printf '%s%s%s' "${base}" "${d1}" "${d2}"
}

plate_from_seed() {
  local seed="$1"
  local n=$((10#${seed} % 100000))
  local letters=(A B C D E F G H I J K L M N O P Q R S T U V W X Y Z)
  printf 'MTR%d%s%02d' "$((n % 10))" "${letters[$((n % 26))]}" "$(((n / 10) % 100))"
}

json_id_by_field() {
  local field="$1"
  local value="$2"
  local id

  if [[ "${LAST_HTTP_OK}" != "true" ]]; then
    log "Busca de id por ${field}=${value} ignorada porque a ultima resposta HTTP nao foi a esperada."
    printf ''
    return 0
  fi

  id="$(jq -er --arg field "${field}" --arg value "${value}" '.[] | select(.[$field] == $value) | .id' <<<"${LAST_BODY}" 2>/dev/null | head -n 1 || true)"
  if [[ -z "${id}" ]]; then
    record_validation_error "Resposta JSON nao contem id para ${field}=${value}."
  fi
  printf '%s' "${id}"
}

json_field() {
  local expression="$1"
  local description="$2"
  local value

  if [[ "${LAST_HTTP_OK}" != "true" ]]; then
    log "Leitura de ${description} ignorada porque a ultima resposta HTTP nao foi a esperada."
    printf ''
    return 0
  fi

  value="$(jq -er "${expression}" <<<"${LAST_BODY}" 2>/dev/null || true)"
  if [[ -z "${value}" ]]; then
    record_validation_error "Resposta JSON nao contem ${description}."
  fi
  printf '%s' "${value}"
}

assert_json_field() {
  local expression="$1"
  if [[ "${LAST_HTTP_OK}" != "true" ]]; then
    log "Validacao JSON ignorada para ${expression} porque a ultima resposta HTTP nao foi a esperada."
    return 0
  fi

  jq -e "${expression}" <<<"${LAST_BODY}" >/dev/null 2>&1 || record_validation_error "Resposta JSON nao satisfez: ${expression}"
}

assert_json_field_if_body() {
  local expression="$1"
  if [[ "${LAST_HTTP_OK}" != "true" ]]; then
    log "Validacao JSON ignorada para ${expression} porque a ultima resposta HTTP nao foi a esperada."
    return 0
  fi

  if [[ -z "${LAST_BODY}" ]]; then
    log "Resposta sem corpo; validacao JSON ignorada para ${expression}."
    return 0
  fi
  assert_json_field "${expression}"
}

assert_metric() {
  local metric="$1"
  if [[ "${LAST_HTTP_OK}" != "true" ]]; then
    log "Validacao da metrica ${metric} ignorada porque a ultima resposta HTTP nao foi a esperada."
    return 0
  fi

  grep -q "${metric}" <<<"${LAST_BODY}" || record_validation_error "Metrica ${metric} nao encontrada em /q/metrics."
}

path_from_url() {
  local url="$1"
  if [[ "${url}" == http://* || "${url}" == https://* ]]; then
    printf '/%s' "${url#*://*/}"
  else
    printf '%s' "${url}"
  fi
}

test_public_and_security() {
  log "Validando endpoints publicos e seguranca."
  api GET "/q/health/live" 200
  api GET "/q/health/ready" 200
  api GET "/q/openapi" 200 "${ADMIN_TOKEN}"
  api GET "/q/metrics" 200 "${ADMIN_TOKEN}"
  if [[ "${LAST_HTTP_OK}" == "true" && -z "${LAST_BODY}" ]]; then
    record_validation_error "/q/metrics retornou corpo vazio."
  fi

  api GET "/usuarios" 401
  api GET "/usuarios" 403 "${RECEPCIONISTA_TOKEN}"
}

test_common_apis() {
  log "Validando APIs de pessoas, usuarios e clientes."

  local pessoa_cpf usuario_cpf cliente_cpf pessoa_id usuario_id cliente_id
  pessoa_cpf="$(cpf_from_seed "$((RUN_SEED + 11))")"
  usuario_cpf="$(cpf_from_seed "$((RUN_SEED + 22))")"
  cliente_cpf="$(cpf_from_seed "$((RUN_SEED + 33))")"

  api POST "/pessoas" 204 "${ADMIN_TOKEN}" "$(jq -cn --arg documento "${pessoa_cpf}" --arg nome "Pessoa ${RUN_LABEL}" '{documento:$documento,nome:$nome}')"
  api GET "/pessoas" 200 "${ADMIN_TOKEN}"
  pessoa_id="$(json_id_by_field "documento" "${pessoa_cpf}")"
  api GET "/pessoas/${pessoa_id}" 200 "${ADMIN_TOKEN}"
  api PUT "/pessoas/${pessoa_id}" 204 "${ADMIN_TOKEN}" "$(jq -cn --arg documento "${pessoa_cpf}" --arg nome "Pessoa Atualizada ${RUN_LABEL}" '{documento:$documento,nome:$nome}')"

  api POST "/usuarios/completos" 204 "${ADMIN_TOKEN}" "$(
    jq -cn --arg documento "${usuario_cpf}" --arg nome "Usuario ${RUN_LABEL}" --arg password "secret" \
      '{documento:$documento,nome:$nome,password:$password,status:"ATIVO",papeis:["recepcionista"]}'
  )"
  api GET "/usuarios" 200 "${ADMIN_TOKEN}"
  usuario_id="$(json_id_by_field "documento" "${usuario_cpf}")"
  api GET "/usuarios/${usuario_id}" 200 "${ADMIN_TOKEN}"
  api GET "/usuarios/completos" 200 "${ADMIN_TOKEN}"
  api GET "/usuarios/completos/${usuario_id}" 200 "${ADMIN_TOKEN}"
  api PUT "/usuarios/completos/${usuario_id}" 204 "${ADMIN_TOKEN}" "$(
    jq -cn --arg documento "${usuario_cpf}" --arg nome "Usuario Atualizado ${RUN_LABEL}" \
      '{documento:$documento,nome:$nome,password:null,status:"INATIVO",papeis:["recepcionista"]}'
  )"
  api DELETE "/usuarios/${usuario_id}" 204 "${ADMIN_TOKEN}"
  api GET "/usuarios/${usuario_id}" 404 "${ADMIN_TOKEN}"

  api POST "/clientes/completos" 204 "${RECEPCIONISTA_TOKEN}" "$(
    jq -cn --arg documento "${cliente_cpf}" --arg nome "Cliente ${RUN_LABEL}" --arg email "cliente-${RUN_SEED}@oficina.local" \
      '{documento:$documento,nome:$nome,email:$email}'
  )"
  api GET "/clientes" 200 "${RECEPCIONISTA_TOKEN}"
  cliente_id="$(json_id_by_field "documento" "${cliente_cpf}")"
  api GET "/clientes/${cliente_id}" 200 "${RECEPCIONISTA_TOKEN}"
  api GET "/clientes/completos" 200 "${RECEPCIONISTA_TOKEN}"
  api GET "/clientes/completos/${cliente_id}" 200 "${RECEPCIONISTA_TOKEN}"
  api PUT "/clientes/completos/${cliente_id}" 204 "${RECEPCIONISTA_TOKEN}" "$(
    jq -cn --arg documento "${cliente_cpf}" --arg nome "Cliente Atualizado ${RUN_LABEL}" --arg email "cliente-atualizado-${RUN_SEED}@oficina.local" \
      '{documento:$documento,nome:$nome,email:$email}'
  )"
  api DELETE "/clientes/completos/${cliente_id}" 204 "${ADMIN_TOKEN}"
  api GET "/clientes/${cliente_id}" 404 "${RECEPCIONISTA_TOKEN}"

  api DELETE "/pessoas/${pessoa_id}" 204 "${ADMIN_TOKEN}"
  api GET "/pessoas/${pessoa_id}" 404 "${ADMIN_TOKEN}"
}

test_catalog_stock_vehicle_apis() {
  log "Validando APIs de veiculos, catalogo e estoque."

  local placa
  local id_inexistente=9223372036854775807
  placa="$(plate_from_seed "${RUN_SEED}")"

  api POST "/veiculos" 204 "${RECEPCIONISTA_TOKEN}" "$(jq -cn --arg placa "${placa}" '{placa:$placa,marca:"Marca Lab",modelo:"Modelo Lab",ano:2026}')"
  api GET "/veiculos/1" 200 "${RECEPCIONISTA_TOKEN}"
  api PUT "/veiculos/1" 204 "${RECEPCIONISTA_TOKEN}" '{"placa":"ABC1234","marca":"11111111111","modelo":"11111111111","ano":11111111}'
  api DELETE "/veiculos/${id_inexistente}" 204 "${ADMIN_TOKEN}"

  api POST "/pecas" 204 "${ADMIN_TOKEN}" "$(jq -cn --arg nome "Peca ${RUN_LABEL}" '{nome:$nome}')"
  api GET "/pecas/1" 200 "${ADMIN_TOKEN}"
  api PUT "/pecas/3" 204 "${ADMIN_TOKEN}" '{"nome":"Tapete"}'
  api DELETE "/pecas/${id_inexistente}" 404 "${ADMIN_TOKEN}"

  api POST "/servicos" 204 "${ADMIN_TOKEN}" "$(jq -cn --arg nome "Servico ${RUN_LABEL}" '{nome:$nome}')"
  api GET "/servicos/1" 200 "${ADMIN_TOKEN}"
  api PUT "/servicos/1" 204 "${ADMIN_TOKEN}" '{"nome":"Troca de oleo"}'
  api DELETE "/servicos/${id_inexistente}" 404 "${ADMIN_TOKEN}"

  api POST "/estoque/acrescentar" 204 "${ADMIN_TOKEN}" '{"id":3,"ordemDeServicoId":null,"quantidade":5.000,"observacao":"Validacao de metricas - entrada"}'
  api POST "/estoque/baixar" 204 "${ADMIN_TOKEN}" '{"id":3,"ordemDeServicoId":null,"quantidade":1.000,"observacao":"Validacao de metricas - saida"}'
  api POST "/estoque/baixar" 409 "${ADMIN_TOKEN}" '{"id":3,"ordemDeServicoId":null,"quantidade":999999.000,"observacao":"Validacao de metricas - conflito esperado"}'
}

test_order_lifecycle() {
  log "Validando ciclo de vida completo da ordem de servico."

  local os_id refused_os_id complete_os_id
  local complete_cliente_cpf complete_placa

  api POST "/estoque/acrescentar" 204 "${ADMIN_TOKEN}" '{"id":1,"ordemDeServicoId":null,"quantidade":5.000,"observacao":"Saldo para ciclo de vida de OS"}'

  api POST "/ordem-de-servico" 200 "${RECEPCIONISTA_TOKEN}" '{"cpfCliente":"50132372037","placaVeiculo":"ABC1234"}'
  os_id="$(json_field '.ordemDeServicoId' 'ordemDeServicoId da OS principal')"
  if [[ -z "${os_id}" ]]; then
    log "Ciclo principal de OS ignorado porque a OS principal nao foi criada."
    return 0
  fi
  log "OS principal criada: ${os_id}"

  api GET "/ordem-de-servico/${os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
  assert_json_field_if_body '.estado == "RECEBIDA"'

  api POST "/ordem-de-servico/${os_id}/iniciar-diagnostico" 204 "${MECANICO_TOKEN}"
  api GET "/ordem-de-servico/${os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
  assert_json_field_if_body '.estado == "EM_DIAGNOSTICO"'

  api POST "/ordem-de-servico/${os_id}/incluir-servico" 204 "${MECANICO_TOKEN}" '{"servicoId":1,"quantidade":1.000,"valorUnitario":120.00}'
  api POST "/ordem-de-servico/${os_id}/incluir-peca" 204 "${MECANICO_TOKEN}" '{"pecaId":1,"quantidade":1.000,"valorUnitario":50.00}'
  api POST "/ordem-de-servico/${os_id}/finalizar-diagnostico" 204 "${MECANICO_TOKEN}"
  api GET "/ordem-de-servico/${os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
  assert_json_field_if_body '.estado == "AGUARDANDO_APROVACAO"'

  if [[ "${SKIP_MAGIC_LINK}" != "true" ]]; then
    api POST "/ordem-de-servico/${os_id}/enviar-link-magico" 200 "${RECEPCIONISTA_TOKEN}" "$(jq -cn --arg email "cliente-${RUN_SEED}@oficina.local" '{email:$email}')"
    local acompanhar aprovar recusar
    acompanhar="$(json_field '.acompanhar' 'link de acompanhamento')"
    aprovar="$(json_field '.aprovar' 'link de aprovacao')"
    recusar="$(json_field '.recusar' 'link de recusa')"
    [[ -n "${acompanhar}" ]] && api GET "$(path_from_url "${acompanhar}")" 200
    [[ -n "${aprovar}" ]] && api GET "$(path_from_url "${aprovar}")" 200
    [[ -n "${recusar}" ]] && api GET "$(path_from_url "${recusar}")" 200
  else
    log "Magic links ignorados por SKIP_MAGIC_LINK=true."
  fi

  api POST "/ordem-de-servico/${os_id}/aprovar" 204 "${ADMIN_TOKEN}"
  api GET "/ordem-de-servico/${os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
  assert_json_field_if_body '.estado == "EM_EXECUCAO"'

  api POST "/ordem-de-servico/${os_id}/finalizar" 204 "${MECANICO_TOKEN}"
  api GET "/ordem-de-servico/${os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
  assert_json_field_if_body '.estado == "FINALIZADA"'

  api POST "/ordem-de-servico/${os_id}/entregar" 204 "${RECEPCIONISTA_TOKEN}"
  api GET "/ordem-de-servico/${os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
  assert_json_field_if_body '.estado == "ENTREGUE"'

  api GET "/ordem-de-servico/${os_id}" 200 "${ADMIN_TOKEN}"
  api GET "/ordem-de-servico/${os_id}/historico-estado" "200,204" "${ADMIN_TOKEN}"
  api GET "/ordem-de-servico?page=0&size=20&sort=criadoEm,desc" 200 "${ADMIN_TOKEN}"
  api GET "/ordem-de-servico?estado=ENTREGUE&page=0&size=20" 200 "${ADMIN_TOKEN}"
  api GET "/ordem-de-servico/abertas-priorizadas" 200 "${ADMIN_TOKEN}"

  api POST "/ordem-de-servico" 200 "${RECEPCIONISTA_TOKEN}" '{"cpfCliente":"50132372037","placaVeiculo":"ABC1234"}'
  refused_os_id="$(json_field '.ordemDeServicoId' 'ordemDeServicoId da OS recusada')"
  if [[ -n "${refused_os_id}" ]]; then
    api POST "/ordem-de-servico/${refused_os_id}/iniciar-diagnostico" 204 "${MECANICO_TOKEN}"
    api POST "/ordem-de-servico/${refused_os_id}/finalizar-diagnostico" 204 "${MECANICO_TOKEN}"
    api POST "/ordem-de-servico/${refused_os_id}/recusar" 204 "${ADMIN_TOKEN}"
    api GET "/ordem-de-servico/${refused_os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
    assert_json_field_if_body '.estado == "EM_DIAGNOSTICO"'
  else
    log "Fluxo de recusa de OS ignorado porque a OS nao foi criada."
  fi

  complete_cliente_cpf="$(cpf_from_seed "$((RUN_SEED + 44))")"
  complete_placa="$(plate_from_seed "$((RUN_SEED + 44))")"
  api POST "/ordem-de-servico/completa" 200 "${RECEPCIONISTA_TOKEN}" "$(
    jq -cn \
      --arg documento "${complete_cliente_cpf}" \
      --arg nome "Cliente OS Completa ${RUN_LABEL}" \
      --arg email "os-completa-${RUN_SEED}@oficina.local" \
      --arg placa "${complete_placa}" \
      '{documentoDoCliente:$documento,nomeDoCliente:$nome,emailDoCliente:$email,placaDoVeiculo:$placa,marcaDoVeiculo:"Marca Lab",modeloDoVeiculo:"Modelo Lab",ano:2026,servicos:[{servicoId:1,quantidade:1.000,valorUnitario:100.00}],pecas:[]}'
  )"
  complete_os_id="$(json_field '.ordemDeServicoId' 'ordemDeServicoId da OS completa')"
  if [[ -n "${complete_os_id}" ]]; then
    api GET "/ordem-de-servico/${complete_os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
    assert_json_field_if_body '.estado == "EM_DIAGNOSTICO"'
  else
    log "Consulta da OS completa ignorada porque a OS nao foi criada."
  fi
}

test_integration_failures() {
  log "Forcando falha esperada de integracao com notificacao."

  local integration_failure_os_id invalid_email

  api POST "/ordem-de-servico" 200 "${RECEPCIONISTA_TOKEN}" '{"cpfCliente":"50132372037","placaVeiculo":"ABC1234"}'
  integration_failure_os_id="$(json_field '.ordemDeServicoId' 'ordemDeServicoId para falha de integracao')"
  if [[ -z "${integration_failure_os_id}" ]]; then
    log "Falha de integracao ignorada porque a OS nao foi criada."
    return 0
  fi
  invalid_email="$(printf 'destino-invalido\nx-oficina-validacao-%s' "${RUN_SEED}")"

  api POST "/ordem-de-servico/${integration_failure_os_id}/enviar-link-magico" "400,500,502,503" "${RECEPCIONISTA_TOKEN}" "$(
    jq -cn --arg email "${invalid_email}" '{email:$email}'
  )"
}

test_order_processing_failures() {
  log "Forcando falhas esperadas de processamento da OS."

  local invalid_transition_os_id stock_failure_os_id stock_failure_cliente_cpf stock_failure_placa

  api POST "/ordem-de-servico" 200 "${RECEPCIONISTA_TOKEN}" '{"cpfCliente":"50132372037","placaVeiculo":"ABC1234"}'
  invalid_transition_os_id="$(json_field '.ordemDeServicoId' 'ordemDeServicoId para transicao invalida')"
  if [[ -n "${invalid_transition_os_id}" ]]; then
    api POST "/ordem-de-servico/${invalid_transition_os_id}/finalizar" 409 "${MECANICO_TOKEN}"
    api GET "/ordem-de-servico/${invalid_transition_os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
    assert_json_field_if_body '.estado == "RECEBIDA"'
  else
    log "Falha de transicao invalida ignorada porque a OS nao foi criada."
  fi

  stock_failure_cliente_cpf="$(cpf_from_seed "$((RUN_SEED + 55))")"
  stock_failure_placa="$(plate_from_seed "$((RUN_SEED + 55))")"
  api POST "/ordem-de-servico/completa" 200 "${RECEPCIONISTA_TOKEN}" "$(
    jq -cn \
      --arg documento "${stock_failure_cliente_cpf}" \
      --arg nome "Cliente OS Falha ${RUN_LABEL}" \
      --arg email "os-falha-${RUN_SEED}@oficina.local" \
      --arg placa "${stock_failure_placa}" \
      '{documentoDoCliente:$documento,nomeDoCliente:$nome,emailDoCliente:$email,placaDoVeiculo:$placa,marcaDoVeiculo:"Marca Lab",modeloDoVeiculo:"Modelo Lab",ano:2026,servicos:[{servicoId:1,quantidade:1.000,valorUnitario:100.00}],pecas:[{pecaId:1,quantidade:999999.000,valorUnitario:50.00}]}'
  )"
  stock_failure_os_id="$(json_field '.ordemDeServicoId' 'ordemDeServicoId para falha de estoque')"
  if [[ -n "${stock_failure_os_id}" ]]; then
    api POST "/ordem-de-servico/${stock_failure_os_id}/finalizar-diagnostico" 409 "${MECANICO_TOKEN}"
    api GET "/ordem-de-servico/${stock_failure_os_id}/estado-atual" "200,204" "${ADMIN_TOKEN}"
    assert_json_field_if_body '.estado == "EM_DIAGNOSTICO"'
  else
    log "Falha de estoque na OS ignorada porque a OS completa nao foi criada."
  fi
}

validate_metrics() {
  log "Validando metricas Prometheus geradas pelo fluxo."
  api GET "/q/metrics" 200 "${ADMIN_TOKEN}"
  assert_metric "os_created_total"
  assert_metric "os_status_transition_total"
  assert_metric "os_status_duration_ms"
  if is_truthy "${FORCAR_FALHAS_INTEGRACAO}"; then
    assert_metric "integration_failures_total"
  fi
  log "Metricas de OS encontradas em /q/metrics."
}

run_validation_once() {
  local iteration="$1"
  ITERATION_ERRORS=0
  prepare_iteration_context "${iteration}"
  ITERATION_ERRORS_FILE="${TMP_DIR}/errors-${RUN_LABEL}.log"
  : > "${ITERATION_ERRORS_FILE}"

  log "Iniciando execucao ${iteration} com RUN_LABEL=${RUN_LABEL}."
  authenticate
  test_public_and_security
  test_common_apis
  test_catalog_stock_vehicle_apis
  test_order_lifecycle
  if is_truthy "${FORCAR_FALHAS_INTEGRACAO}"; then
    test_integration_failures
  else
    log "Falhas de integracao ignoradas por FORCAR_FALHAS_INTEGRACAO=false."
  fi
  if is_truthy "${FORCAR_FALHAS_OS}"; then
    test_order_processing_failures
  else
    log "Falhas de processamento da OS ignoradas por FORCAR_FALHAS_OS=false."
  fi
  validate_metrics
  ITERATION_ERRORS="$(count_file_lines "${ITERATION_ERRORS_FILE}")"
  TOTAL_ERRORS="$(count_file_lines "${ERRORS_FILE}")"
  if [[ "${ITERATION_ERRORS}" -gt 0 ]]; then
    log "Execucao ${iteration} concluida com ${ITERATION_ERRORS} erro(s) contabilizado(s)."
  else
    log "Execucao ${iteration} concluida sem erros contabilizados."
  fi
}

main() {
  if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
  fi

  require_cmd curl
  require_cmd jq
  validate_loop_config
  configure_access_mode
  mkdir -p "${TMP_DIR}"
  : > "${ERRORS_FILE}"
  trap cleanup EXIT
  trap on_interrupt INT TERM

  log "Configuracao: MODO_ACESSO=${MODO_ACESSO}, APP_BASE_URL=${APP_BASE_URL}, AUTH_MODE=${AUTH_MODE}, EKS_CLUSTER_NAME=${EKS_CLUSTER_NAME}, EXECUCOES=${EXECUCOES}, RUN_FOREVER=${RUN_FOREVER}, FORCAR_FALHAS_INTEGRACAO=${FORCAR_FALHAS_INTEGRACAO}, FORCAR_FALHAS_OS=${FORCAR_FALHAS_OS}."
  start_port_forward

  local iteration=1
  if is_truthy "${RUN_FOREVER}"; then
    log "Modo continuo ativo. Pressione Ctrl+C para interromper."
    while true; do
      run_validation_once "${iteration}"
      iteration=$((iteration + 1))
      if [[ "${PAUSE_SECONDS}" -gt 0 ]]; then
        sleep "${PAUSE_SECONDS}"
      fi
    done
  fi

  for ((iteration = 1; iteration <= EXECUCOES; iteration++)); do
    run_validation_once "${iteration}"
    if [[ "${iteration}" -lt "${EXECUCOES}" && "${PAUSE_SECONDS}" -gt 0 ]]; then
      sleep "${PAUSE_SECONDS}"
    fi
  done

  if [[ "${ENABLE_PORT_FORWARD}" == "true" ]]; then
    TOTAL_ERRORS="$(count_file_lines "${ERRORS_FILE}")"
    log "Validacao concluida com ${TOTAL_ERRORS} erro(s) contabilizado(s). Port-forward mantido em ${APP_BASE_URL}; PIDs/logs em ${PF_DIR}."
  else
    TOTAL_ERRORS="$(count_file_lines "${ERRORS_FILE}")"
    log "Validacao concluida em ${APP_BASE_URL} com ${TOTAL_ERRORS} erro(s) contabilizado(s)."
  fi
}

main "$@"
