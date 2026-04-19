#!/usr/bin/env bash
set -euo pipefail

IMAGE_REF="${1:-${IMAGE_REF:-}}"
AWS_REGION="${AWS_REGION:-us-east-1}"

if [[ -z "${IMAGE_REF}" ]]; then
  echo "Uso: $(basename "$0") <image-ref>" >&2
  exit 1
fi

if ! command -v aws >/dev/null 2>&1; then
  echo "Comando obrigatorio nao encontrado: aws" >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Comando obrigatorio nao encontrado: docker" >&2
  exit 1
fi

registry="${IMAGE_REF%%/*}"
password="$(aws --region "${AWS_REGION}" ecr get-login-password)"
docker login --username AWS --password-stdin "${registry}" <<<"${password}"
docker push "${IMAGE_REF}"

echo "Imagem publicada: ${IMAGE_REF}"
