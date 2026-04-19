#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

AWS_REGION="${AWS_REGION:-us-east-1}"
ECR_REPOSITORY_NAME="${ECR_REPOSITORY_NAME:-oficina}"
ECR_REPOSITORY_URL="${ECR_REPOSITORY_URL:-}"
APP_IMAGE_TAG="${APP_IMAGE_TAG:-${IMAGE_TAG:-}}"

pom_version() {
  sed -n '0,/<version>[[:space:]]*/{
    s/.*<version>[[:space:]]*\([^<][^<]*\)[[:space:]]*<\/version>.*/\1/p
  }' "${REPO_ROOT}/pom.xml" | head -n 1
}

set_output() {
  local name="$1"
  local value="$2"

  if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
    printf '%s=%s\n' "${name}" "${value}" >> "${GITHUB_OUTPUT}"
  fi
}

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

if [[ -z "${APP_IMAGE_TAG}" ]]; then
  APP_IMAGE_TAG="$(pom_version)"
fi

require_non_empty "${APP_IMAGE_TAG}" "APP_IMAGE_TAG"

if [[ -z "${ECR_REPOSITORY_URL}" ]]; then
  require_cmd aws
  require_non_empty "${AWS_REGION}" "AWS_REGION"
  require_non_empty "${ECR_REPOSITORY_NAME}" "ECR_REPOSITORY_NAME"

  ECR_REPOSITORY_URL="$(
    aws --region "${AWS_REGION}" ecr describe-repositories \
      --repository-names "${ECR_REPOSITORY_NAME}" \
      --query 'repositories[0].repositoryUri' \
      --output text
  )"
fi

require_non_empty "${ECR_REPOSITORY_URL}" "ECR_REPOSITORY_URL"

if [[ -z "${ECR_REPOSITORY_NAME}" || "${ECR_REPOSITORY_NAME}" == "oficina" ]]; then
  ECR_REPOSITORY_NAME="${ECR_REPOSITORY_URL#*/}"
fi

IMAGE_REF="${ECR_REPOSITORY_URL}:${APP_IMAGE_TAG}"

set_output repository_name "${ECR_REPOSITORY_NAME}"
set_output repository_url "${ECR_REPOSITORY_URL}"
set_output image_tag "${APP_IMAGE_TAG}"
set_output image_ref "${IMAGE_REF}"

printf '%s\n' "${IMAGE_REF}"
