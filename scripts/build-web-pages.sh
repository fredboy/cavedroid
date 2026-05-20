#!/usr/bin/env bash

# Build the release web bundle and stage it for GitHub Pages deployment.
# Produces pages-dist/ at the repo root, ready to be uploaded as a Pages
# artifact (excludes WEB-INF/, which is only used by the embedded dev server).
#
# Usage:
#   ./scripts/build-web-pages.sh

set -euo pipefail

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root"
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

# The html build doesn't need signing, but a few other modules in the graph
# read keystore.properties at configure time. Mock it if absent so the build
# works on CI / fresh clones without a real keystore.
if [[ ! -f keystore.properties ]]; then
  echo ">> Mocking keystore.properties (not required for web build)"
  echo "releaseKeystorePath=mock" > keystore.properties
fi

out_dir="pages-dist"
rm -rf "$out_dir"

echo ">> Running html:buildJsRelease"
./gradlew html:buildJsRelease

src_dir="html/build/dist/webapp"
if [[ ! -d "$src_dir" ]]; then
  echo >&2 "error: expected web bundle at $src_dir but it does not exist"
  exit 1
fi

echo ">> Staging $src_dir -> $out_dir/ (excluding WEB-INF/)"
mkdir -p "$out_dir"
# rsync is available on ubuntu-latest runners and most dev machines; cp -a
# would be a fallback but doesn't support --exclude.
rsync -a --exclude='WEB-INF' "$src_dir/" "$out_dir/"

echo ">> Done. Contents:"
ls -la "$out_dir"
