#!/usr/bin/env bash
set -euo pipefail

JAR=$(ls target/user-management-cloud-*.jar | head -n 1)

if ! command -v eb > /dev/null; then
  echo "ERROR: AWS EB CLI not installed. Install with: pip install awsebcli" >&2
  exit 1
fi

# Build the jar if it doesn't exist
if [[ ! -f "${JAR}" ]]; then
  mvn -DskipTests clean package
  JAR=$(ls target/user-management-cloud-*.jar | head -n 1)
fi

# Create a new application version and deploy
VERSION=$(date +%Y%m%d%H%M%S)
eb init -p "Corretto 21" user-management-cloud --region "${AWS_REGION:-eu-north-1}"
eb use "${EB_ENV_NAME}"
eb deploy --staged --label "${VERSION}"
