#!/usr/bin/env bash

set -euo pipefail

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root (no ./gradlew here)"
  exit 1
fi

if [[ -z "${1:-}" ]]; then
  echo "usage: $0 versionName"
  exit 1
fi

new_version=$1

sed -i 's/\(cavedroid.versionName=\).*/\1'"$new_version"'/' gradle.properties
sed -i 's/\(cavedroid.versionCode=\)\([0-9]*\)/echo "\1$((\2+1))"/ge' gradle.properties
sed -i 's/\(const val VERSION = \)\".*\"/\1\"'"$new_version"'\"/' core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt

git add gradle.properties core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt
