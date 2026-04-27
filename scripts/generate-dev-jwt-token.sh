#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JWT_DIR="${JWT_DIR:-$ROOT_DIR/src/main/resources/jwt}"
PRIVATE_KEY="${JWT_DIR}/privateKey.pem"

ISSUER="${OFICINA_AUTH_ISSUER:-oficina-api}"
AUDIENCE="${OFICINA_AUTH_AUDIENCE:-oficina-app}"
SCOPE="${OFICINA_AUTH_SCOPE:-oficina-app}"
KEY_ID="${OFICINA_AUTH_KEY_ID:-oficina-lab-rsa}"
SUBJECT="84191404067"
TTL_SECONDS=3600
ROLES=("administrativo" "mecanico" "recepcionista")

usage() {
  cat <<'EOF'
Uso: ./scripts/generate-dev-jwt-token.sh [opcoes]

Gera um JWT de desenvolvimento compatível com o perfil dev do oficina-app
e imprime o token no final para uso no Swagger UI.

Opcoes:
  --subject <cpf>      Define o subject/sub/upn do token. Default: 84191404067
  --roles <lista>      Lista separada por virgula. Ex: administrativo,mecanico
  --ttl <segundos>     Tempo de expiracao do token. Default: 3600
  --help               Exibe esta ajuda

Variaveis de ambiente opcionais:
  OFICINA_AUTH_ISSUER
  OFICINA_AUTH_AUDIENCE
  OFICINA_AUTH_SCOPE
  OFICINA_AUTH_KEY_ID
  JWT_DIR
EOF
}

require_command() {
  local command_name="$1"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "Comando obrigatorio nao encontrado: ${command_name}" >&2
    exit 1
  fi
}

validate_role() {
  local role="$1"
  case "$role" in
    administrativo|mecanico|recepcionista) ;;
    *)
      echo "Papel invalido: ${role}. Use administrativo, mecanico e/ou recepcionista." >&2
      exit 1
      ;;
  esac
}

parse_roles() {
  local roles_csv="$1"
  local parsed_roles=()
  IFS=',' read -r -a parsed_roles <<<"$roles_csv"

  if [[ "${#parsed_roles[@]}" -eq 0 ]]; then
    echo "Informe pelo menos um papel em --roles." >&2
    exit 1
  fi

  ROLES=()
  for role in "${parsed_roles[@]}"; do
    role="${role//[[:space:]]/}"
    if [[ -z "$role" ]]; then
      continue
    fi
    validate_role "$role"
    ROLES+=("$role")
  done

  if [[ "${#ROLES[@]}" -eq 0 ]]; then
    echo "Informe pelo menos um papel valido em --roles." >&2
    exit 1
  fi
}

base64url() {
  openssl base64 -A | tr '+/' '-_' | tr -d '='
}

json_array() {
  local values=("$@")
  local json="["
  local first="true"

  for value in "${values[@]}"; do
    if [[ "$first" == "true" ]]; then
      first="false"
    else
      json+=","
    fi
    json+="\"${value}\""
  done

  json+="]"
  printf '%s' "$json"
}

ensure_dev_keys() {
  if [[ -f "$PRIVATE_KEY" ]]; then
    return
  fi

  echo "Chaves JWT de desenvolvimento nao encontradas em ${JWT_DIR}. Gerando par local..." >&2
  "${ROOT_DIR}/scripts/generate-dev-jwt-keys.sh" >&2
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --subject)
      SUBJECT="${2:-}"
      shift 2
      ;;
    --roles)
      parse_roles "${2:-}"
      shift 2
      ;;
    --ttl)
      TTL_SECONDS="${2:-}"
      shift 2
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Opcao invalida: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

require_command openssl

if [[ -z "$SUBJECT" ]]; then
  echo "O subject do token nao pode ser vazio." >&2
  exit 1
fi

if ! [[ "$TTL_SECONDS" =~ ^[0-9]+$ ]] || [[ "$TTL_SECONDS" -le 0 ]]; then
  echo "O TTL deve ser um inteiro positivo em segundos." >&2
  exit 1
fi

ensure_dev_keys

if [[ ! -f "$PRIVATE_KEY" ]]; then
  echo "Chave privada JWT nao encontrada em ${PRIVATE_KEY}." >&2
  exit 1
fi

issued_at="$(date +%s)"
expires_at="$((issued_at + TTL_SECONDS))"
roles_json="$(json_array "${ROLES[@]}")"

header_json="$(printf '{"alg":"RS256","typ":"JWT","kid":"%s"}' "$KEY_ID")"
payload_json="$(printf '{"iss":"%s","sub":"%s","upn":"%s","aud":"%s","scope":"%s","groups":%s,"iat":%s,"exp":%s}' \
  "$ISSUER" "$SUBJECT" "$SUBJECT" "$AUDIENCE" "$SCOPE" "$roles_json" "$issued_at" "$expires_at")"

header_b64="$(printf '%s' "$header_json" | base64url)"
payload_b64="$(printf '%s' "$payload_json" | base64url)"
signing_input="${header_b64}.${payload_b64}"
signature_b64="$(printf '%s' "$signing_input" | openssl dgst -binary -sha256 -sign "$PRIVATE_KEY" | base64url)"

echo "Token gerado para subject=${SUBJECT}, roles=$(IFS=,; echo "${ROLES[*]}"), expira_em=${TTL_SECONDS}s" >&2
printf '%s.%s\n' "$signing_input" "$signature_b64"
