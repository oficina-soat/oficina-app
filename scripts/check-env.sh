#!/usr/bin/env bash
set -euo pipefail

require_aws=false

fail() {
  echo "ERRO: $1" >&2
  exit 1
}

ensure_command() {
  local command_name="$1"
  command -v "$command_name" >/dev/null 2>&1 || fail "comando obrigatorio ausente: $command_name"
}

for arg in "$@"; do
  case "$arg" in
    --require-aws)
      require_aws=true
      ;;
    *)
      echo "Uso: $0 [--require-aws]" >&2
      exit 1
      ;;
  esac
done

echo "Validando Maven Wrapper..."
test -x ./mvnw || {
  echo "ERRO: ./mvnw nao encontrado ou sem permissao de execucao." >&2
  echo "Execute: chmod +x ./mvnw" >&2
  exit 1
}

./mvnw -version >/dev/null

echo "Validando Docker..."
ensure_command docker
docker ps >/dev/null || fail "nao foi possivel acessar o daemon do Docker. Verifique se o servico esta ativo e se o usuario atual tem permissao no socket."

if [ -f docker-compose.yml ] || [ -f compose.yml ] || [ -f compose.yaml ]; then
  echo "Validando Docker Compose..."
  docker compose ps >/dev/null || fail "nao foi possivel executar 'docker compose ps'."
fi

if [ "$require_aws" = true ]; then
  echo "Validando identidade AWS..."
  ensure_command aws
  aws sts get-caller-identity >/dev/null || fail "nao foi possivel validar a identidade AWS atual."
else
  echo "AWS nao foi validada. Use --require-aws quando a tarefa depender de ambiente remoto."
fi

echo "Ambiente OK"
