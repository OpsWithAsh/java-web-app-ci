#!/bin/bash
echo "Starting OWASP Dependency Check..."

DC_VERSION="latest"
DC_DIR="./dependency-check"

mkdir -p $DC_DIR
docker run --rm \
  -v $(pwd):/src \
  -v $DC_DIR:/report \
  owasp/dependency-check:$DC_VERSION \
  --project MyApp \
  --scan /src \
  --format "ALL" \
  --out /report

echo "Dependency Check report saved to $DC_DIR"
