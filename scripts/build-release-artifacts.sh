#!/usr/bin/env bash

# Build signed release artifacts for a given version and stage them in
# release-A.B.C/ for upload. Requires keystore.properties to be present.
#
# Usage:
#   ./scripts/build-release-artifacts.sh A.B.C [--skip-legacy]

set -euo pipefail

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root"
  exit 1
fi

if [[ -z "${1:-}" ]]; then
  echo "usage: $0 A.B.C [--skip-legacy]"
  exit 1
fi

version=$1
shift || true

skip_legacy=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-legacy) skip_legacy=1; shift ;;
    *) echo >&2 "unknown arg: $1"; exit 1 ;;
  esac
done

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo >&2 "error: version must be A.B.C (got '$version')"
  exit 1
fi

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  java_bin="$JAVA_HOME/bin/java"
else
  java_bin="java"
fi
java_version=$("$java_bin" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\1/p')
if [[ "$java_version" != "17" ]]; then
  echo ">> Java 17 required; found '${java_version:-unknown}' via $java_bin (JAVA_HOME=${JAVA_HOME:-(unset)})"
  exit 1
fi
echo ">> Java 17 detected (via $java_bin)"

if [[ ! -f keystore.properties ]]; then
  echo >&2 "error: keystore.properties is required for signed release builds"
  exit 1
fi

release_dir="release-$version"
if [[ -e "$release_dir" ]]; then
  echo >&2 "error: '$release_dir/' already exists; remove it first"
  exit 1
fi
mkdir "$release_dir"

echo ">> Running ktlint + main builds (Android foss release, desktop Linux/Win, web)"
./gradlew clean ktlintCheck \
  android:assembleFossRelease \
  desktop:packageLinuxX64 \
  desktop:packageWinX64 \
  html:packageWebDist

echo ">> Staging main artifacts in $release_dir/"
cp "android/build/outputs/apk/foss/release/android-foss-release.apk" "$release_dir/android-foss-$version.apk"
cp "desktop/build/dist/cavedroid-linuxX64.zip" "$release_dir/linux-x86_64-$version.zip"
cp "desktop/build/dist/cavedroid-winX64.zip" "$release_dir/win-x86_64-$version.zip"
cp "html/build/dist/cavedroid-web-$version.zip" "$release_dir/web-$version.zip"

# Legacy Android (best-effort). Non-fatal: this version may not support it.
if [[ $skip_legacy -eq 0 && -f legacy-migration.patch ]]; then
  echo ">> Found legacy-migration.patch — attempting legacy Android build"
  patched_files=$(grep -E '^\+\+\+ b/' legacy-migration.patch | sed 's|^+++ b/||' || true)
  if git apply --check legacy-migration.patch 2>/dev/null; then
    git apply legacy-migration.patch
    if ./gradlew clean android:assembleFossRelease; then
      cp "android/build/outputs/apk/foss/release/android-foss-release.apk" \
         "$release_dir/android-legacy-foss-$version.apk"
      echo ">> Legacy build succeeded: $release_dir/android-legacy-foss-$version.apk"
    else
      echo ">> Legacy gradle build failed — release continues without legacy APK"
    fi
    # Revert patched files; up-version.sh touched different files.
    # shellcheck disable=SC2086
    git checkout -- $patched_files
  else
    echo ">> legacy-migration.patch does not apply cleanly — skipping legacy build"
  fi
fi

echo ">> Artifacts ready in $release_dir/"
ls -lh "$release_dir/"
