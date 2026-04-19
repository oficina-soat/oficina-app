#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE_REF="${1:-${IMAGE_REF:-}}"

if [[ -z "${IMAGE_REF}" ]]; then
  echo "Uso: $(basename "$0") <image-ref> [docker-build-args...]" >&2
  exit 1
fi

shift || true

docker build \
  --build-arg QUARKUS_DATASOURCE_DB_KIND="${QUARKUS_DATASOURCE_DB_KIND:-postgresql}" \
  --build-arg QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT="${QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT:-no-file}" \
  -t "${IMAGE_REF}" \
  "$@" \
  "${ROOT_DIR}"

echo "Imagem gerada: ${IMAGE_REF}"
