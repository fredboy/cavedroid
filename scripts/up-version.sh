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

sed -i 's/\(const val versionName = \)\".*\"/\1\"'"$new_version"'\"/g' buildSrc/src/main/kotlin/ApplicationInfo.kt
sed -i 's/\(\s*const val versionCode = \)\([0-9]*\)/echo "\1$((\2+1))"/ge' buildSrc/src/main/kotlin/ApplicationInfo.kt
sed -i 's/\(const val VERSION = \)\".*\"/\1\"'"$new_version"'\"/' core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt

git add buildSrc/src/main/kotlin/ApplicationInfo.kt core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt
